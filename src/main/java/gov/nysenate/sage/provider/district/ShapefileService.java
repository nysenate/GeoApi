package gov.nysenate.sage.provider.district;

import gov.nysenate.sage.dao.provider.shapefile.ShapefileDao;
import gov.nysenate.sage.dao.provider.shapefile.ShapefileTypeDao;
import gov.nysenate.sage.model.district.*;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.result.IntersectResult;
import gov.nysenate.sage.model.result.MapResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.service.ImmutableCache;
import gov.nysenate.sage.service.district.DistrictCodeCache;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Used to return district maps for state level districts.
 */
@Service
public class ShapefileService {
    private final ShapefileTypeDao typeDao;
    private final ShapefileDao shapefileDao;
    private final ImmutableCache<DistrictType, DistrictTableInfo> typeInfoCache;
    @Getter
    private final DistrictCodeCache<DistrictMap> mapCache;

    @Autowired
    public ShapefileService(ShapefileTypeDao typeDao, ShapefileDao shapefileDao) {
        this.typeDao = typeDao;
        this.shapefileDao = shapefileDao;
        // Order matters: mapCache relies on typeInfoCache indirectly.
        // It needs to be created first so that it will be refreshed first.
        this.typeInfoCache = new ImmutableCache<>(() -> this.typeDao.getTableInfos().stream()
                .collect(Collectors.toMap(DistrictTableInfo::type, Function.identity())));
        this.mapCache = new DistrictCodeCache<>(this::getGeometryMap);
    }

    private Map<String, DistrictMap> getGeometryMap(DistrictType type) {
        DistrictTableInfo tableInfo = typeInfoCache.get(type);
        if (tableInfo == null) {
            return null;
        }
        Set<DistrictMap> maps = shapefileDao.getDistrictMaps(tableInfo);
        return maps.stream().collect(Collectors.toMap(DistrictMap::getDistrictCode, Function.identity()));
    }

    /** Provides a district map given a specific type and district */
    public MapResult getMapResult(DistrictType districtType, String code) {
        if (code == null || code.isBlank()) {
            return new MapResult(ResultStatus.MISSING_DISTRICT_CODE);
        }
        DistrictMap map = mapCache.getData(districtType, code);
        if (map == null) {
            return new MapResult(ResultStatus.NO_MAP_RESULT);
        }
        return new MapResult(map);
    }

    /**
     * Handle intersect requests and executes functions based on settings in the supplied request.
     * @param sourceType type of base map.
     * @param sourceId code of base map.
     * @param intersectWith other type to show overlaps with.
     * @return Maps of this intersection.
     */
    public IntersectResult getIntersectResult(DistrictType sourceType, String sourceId, DistrictType intersectWith) {
        DistrictMap sourceMap = mapCache.getData(sourceType, sourceId);
        DistrictTableInfo baseInfo = typeInfoCache.get(sourceType);
        if (baseInfo == null) {
            throw new NoShapefileForDistrictTypeException(sourceType);
        }
        DistrictTableInfo intersectWithInfo = typeInfoCache.get(intersectWith);
        if (intersectWithInfo == null) {
            throw new NoShapefileForDistrictTypeException(intersectWith);
        }
        List<IntersectMap> overlaps = shapefileDao.getDistrictOverlap(baseInfo, intersectWithInfo, sourceId)
                .stream().map(info -> {
                    var intersectMap = new IntersectMap(intersectWith, info.code());
                    intersectMap.setMapGeoJson(info.geoJson());
                    intersectMap.setArea(info.area());
                    intersectMap.setFullMapGeoJson(mapCache.getData(intersectWith, info.code()).getMapGeoJson());
                    return intersectMap;
                }).toList();
        return new IntersectResult(sourceMap, intersectWith, overlaps);
    }

    public Boolean cleanMaps(DistrictType type) {
        Boolean result = shapefileDao.cleanMaps(typeDao.getDistrictTypeInfo(type));
        mapCache.refresh();
        return result;
    }

    public DistrictInfo getDistrictInfo(Geocode geocode, Set<DistrictType> districtTypes) {
        Set<DistrictTableInfo> tableInfoSet = districtTypes.stream().map(typeInfoCache::get)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<DistrictType, String> typeToCodeMap = shapefileDao.getCodes(geocode.point(), tableInfoSet);
        return new DistrictInfo(typeToCodeMap, geocode.accuracy());
    }
}
