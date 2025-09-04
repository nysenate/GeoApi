package gov.nysenate.sage.provider.district;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import gov.nysenate.sage.dao.model.county.CountyDao;
import gov.nysenate.sage.dao.model.townCity.TownCityDao;
import gov.nysenate.sage.dao.provider.shapefile.SqlShapefileDao;
import gov.nysenate.sage.model.district.*;
import gov.nysenate.sage.model.result.IntersectResult;
import gov.nysenate.sage.model.result.MapListResult;
import gov.nysenate.sage.model.result.MapResult;
import gov.nysenate.sage.model.result.ResultStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;

/**
 * Used to return district maps for state level districts.
 */
@Service
public class ShapefileService {
    private final SqlShapefileDao sqlShapefileDao;
    private final CountyDao countyDao;
    private final TownCityDao townCityDao;

    @Autowired
    public ShapefileService(SqlShapefileDao sqlShapefileDao, CountyDao countyDao, TownCityDao townCityDao) {
        this.sqlShapefileDao = sqlShapefileDao;
        this.countyDao = countyDao;
        this.townCityDao = townCityDao;
    }

    /** Provides a district map given a specific district */
    public MapResult getDistrictMap(DistrictType districtType, String code) {
        if (code == null || code.isBlank()) {
            return new MapResult(ResultStatus.MISSING_DISTRICT_CODE);
        }
        DistrictMap map = sqlShapefileDao.getDistrictMap(districtType, code);
        if (map == null) {
            return new MapResult(ResultStatus.NO_MAP_RESULT);
        }
        return new MapResult(map);
    }

    /** Provides a collection of all district maps for a given type */
    public MapListResult getDistrictMaps(DistrictType districtType) {
        SortedSet<DistrictMap> mapSet = sqlShapefileDao.getDistrictMaps(districtType);
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
    public IntersectResult getIntersectionResult(DistrictType sourceType, String sourceId, DistrictType intersectWith) {
        DistrictMap sourceMap = sqlShapefileDao.getDistrictMap(sourceType, sourceId);
        // We only need the overlap for the specified intersect type
        List<IntersectMap> overlaps = sqlShapefileDao.getDistrictOverlap(sourceType, intersectWith, sourceId);
        return new IntersectResult(sourceMap, intersectWith, overlaps);
    }

    public Multimap<County, TownCity> getCountyToTownCityMap() {
        Set<String> countyCodes = getDistrictMaps(DistrictType.COUNTY).getDistrictMaps().stream()
                .map(DistrictMetadata::getDistrictCode).collect(Collectors.toSet());
        Multimap<County, TownCity> result = HashMultimap.create();
        for (String countyCode : countyCodes) {
            Set<TownCity> currTownCities = sqlShapefileDao.getDistrictOverlap(
                    DistrictType.COUNTY, DistrictType.TOWN_CITY, countyCode
            ).stream().map(DistrictMetadata::getDistrictCode).map(townCityDao::getTownCityByCode)
                    .collect(Collectors.toSet());
            result.putAll(countyDao.getCountyByCode(countyCode), currTownCities);
        }
        return result;
    }
}
