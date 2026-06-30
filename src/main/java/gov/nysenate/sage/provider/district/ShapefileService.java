package gov.nysenate.sage.provider.district;

import com.google.common.collect.ImmutableSortedMap;
import gov.nysenate.sage.dao.provider.SingleDistrictService;
import gov.nysenate.sage.dao.provider.shapefile.ShapefileDao;
import gov.nysenate.sage.dao.provider.shapefile.ShapefileTypeDao;
import gov.nysenate.sage.model.district.*;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.result.IntersectResult;
import gov.nysenate.sage.model.result.MapListResult;
import gov.nysenate.sage.model.result.MapResult;
import gov.nysenate.sage.model.result.ResultStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Used to return district maps for state level districts.
 */
@Service
public class ShapefileService implements SingleDistrictService {
    private final ShapefileTypeDao typeDao;
    private final ShapefileDao shapefileDao;
    private ImmutableSortedMap<DistrictType, DistrictTableInfo> typeInfoCache;
    private ImmutableSortedMap<DistrictType, SortedSet<DistrictMap>> districtMapCache;

    @Autowired
    public ShapefileService(ShapefileTypeDao typeDao, ShapefileDao shapefileDao) {
        this.typeDao = typeDao;
        this.shapefileDao = shapefileDao;
    }

    /**
     * Caches all the district maps from the database.
     */
    @PostConstruct
    public void cacheDistrictGeometryData() {
        this.typeInfoCache = ImmutableSortedMap.copyOf(typeDao.getTableInfos().stream()
                .collect(Collectors.toMap(DistrictTableInfo::type, Function.identity())));
        var tempMap = new HashMap<DistrictType, SortedSet<DistrictMap>>();
        for (DistrictTableInfo tableInfo : typeInfoCache.values()) {
            tempMap.put(tableInfo.type(), shapefileDao.getDistrictMaps(tableInfo));
        }
        this.districtMapCache = ImmutableSortedMap.copyOf(tempMap);
    }

    @Override
    public SingleDistrict getSingleDistrict(DistrictType type, String code) {
        DistrictMap map = getDistrictMap(type, code);
        String name = map == null ? null : map.getDistrictName();
        return new SingleDistrict(code, name);
    }

    /** Provides a district map given a specific district */
    public MapResult getMapResult(DistrictType districtType, String code) {
        if (code == null || code.isBlank()) {
            return new MapResult(ResultStatus.MISSING_DISTRICT_CODE);
        }
        DistrictMap map = getDistrictMap(districtType, code);
        if (map == null) {
            return new MapResult(ResultStatus.NO_MAP_RESULT);
        }
        return new MapResult(map);
    }

    /** Provides a collection of all district maps for a given type */
    public MapListResult getDistrictMaps(DistrictType districtType) {
        SortedSet<DistrictMap> mapSet = districtMapCache.get(districtType);
        if (mapSet == null) {
            return new MapListResult(ResultStatus.NO_MAP_RESULT);
        }
        return new MapListResult(mapSet);
    }

    /**
     * Handle intersect requests and executes functions based on settings in the supplied request.
     * @param sourceType type of base map.
     * @param sourceId code of base map.
     * @param intersectWith other type to show overlaps with.
     * @return Maps of this intersection.
     */
    public IntersectResult getIntersectResult(DistrictType sourceType, String sourceId, DistrictType intersectWith) {
        DistrictMap sourceMap = getDistrictMap(sourceType, sourceId);
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
                    SingleDistrict districtData = getSingleDistrict(intersectWith, info.code());
                    var intersectMap = new IntersectMap(intersectWith, districtData);
                    info.polygons().forEach(intersectMap::addPolygon);
                    intersectMap.setArea(info.area());
                    intersectMap.setFullMapPolygons(getDistrictMap(intersectWith, info.code()).getPolygons());
                    return intersectMap;
                }).toList();
        return new IntersectResult(sourceMap, intersectWith, overlaps);
    }

    public Boolean cleanMaps(DistrictType type) {
        Boolean result = shapefileDao.cleanMaps(typeDao.getDistrictTypeInfo(type));
        cacheDistrictGeometryData();
        return result;
    }

    public List<DistrictType> getTypes() {
        return List.copyOf(typeInfoCache.keySet());
    }

    public DistrictInfo getDistrictInfo(Geocode geocode, Set<DistrictType> districtTypes) {
        Set<DistrictTableInfo> tableInfoSet = districtTypes.stream().map(type -> typeInfoCache.get(type))
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<DistrictType, String> typeToCodeMap = shapefileDao.getCodes(geocode.point(), tableInfoSet);
        Map<DistrictType, SingleDistrict> typeToDistrictMap = typeToCodeMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> getSingleDistrict(entry.getKey(), entry.getValue())
                ));
        return new DistrictInfo(typeToDistrictMap, geocode.accuracy());
    }

    private DistrictMap getDistrictMap(DistrictType type, String code) {
        SortedSet<DistrictMap> maps = districtMapCache.get(type);
        if (maps == null) {
            return null;
        }
        return maps.stream().filter(dMap -> dMap.getDistrictCode().equalsIgnoreCase(code))
                .findFirst().orElse(null);
    }
}
