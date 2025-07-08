package gov.nysenate.sage.provider.district;

import gov.nysenate.sage.dao.model.county.CountyDao;
import gov.nysenate.sage.dao.provider.district.SqlShapefileDao;
import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.IntersectResult;
import gov.nysenate.sage.model.result.MapListResult;
import gov.nysenate.sage.model.result.MapResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.util.FormatUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShapefileService implements MapService {
    private final SqlShapefileDao sqlShapefileDao;

    private final CountyDao countyDao;

    @Autowired
    public ShapefileService(SqlShapefileDao sqlShapefileDao, CountyDao countyDao) {
        this.sqlShapefileDao = sqlShapefileDao;
        this.countyDao = countyDao;
    }

    /** {@inheritDoc} */
    @Override
    public MapResult getDistrictMap(DistrictType districtType, String code) {
        var mapResult = new MapResult();
        if (code != null && !code.isEmpty()) {
            code = FormatUtil.trimLeadingZeroes(code);
            DistrictMap map = sqlShapefileDao.getDistrictMap(districtType, code);
            if (map != null) {
                // For COVID links
                if (districtType.equals(DistrictType.COUNTY)) {
                    map.setLink(countyDao.getCountyBySenateCode(Integer.parseInt(code)).link());
                }
                mapResult.setDistrictMap(map);
                mapResult.setStatusCode(ResultStatus.SUCCESS);
            }
            else {
                mapResult.setStatusCode(ResultStatus.NO_MAP_RESULT);
            }
        }
        else {
            mapResult.setStatusCode(ResultStatus.MISSING_DISTRICT_CODE);
        }
        return mapResult;
    }

    /** {@inheritDoc} */
    @Override
    public MapListResult getDistrictMaps(DistrictType districtType) {
        var mapResult = new MapListResult();
        List<DistrictMap> mapCollection = sqlShapefileDao.getDistrictMaps(districtType);
        if (mapCollection != null) {
            mapResult.setDistrictMaps(mapCollection);
            mapResult.setStatusCode(ResultStatus.SUCCESS);
            if (districtType.equals(DistrictType.COUNTY)) {
                for (DistrictMap map : mapCollection) {
                    map.setLink(countyDao.getCountyBySenateCode(Integer.parseInt(map.getDistrictCode())).link());
                }
            }
        }
        else {
            mapResult.setStatusCode(ResultStatus.NO_MAP_RESULT);
        }
        return mapResult;
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
        List<DistrictMap> overlaps = sqlShapefileDao.getDistrictOverlap(sourceType, intersectWith, sourceId);
        var result = new IntersectResult(sourceMap, intersectWith, overlaps);
        result.setResultTime();
        return result;
    }
}
