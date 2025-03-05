package gov.nysenate.sage.provider.district;

import gov.nysenate.sage.dao.model.county.CountyDao;
import gov.nysenate.sage.dao.provider.district.SqlShapefileDao;
import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictOverlap;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.IntersectResult;
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
        var mapResult = new MapResult(MapSource.SHAPEFILE);
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
    public MapResult getDistrictMaps(DistrictType districtType) {
        MapResult mapResult = new MapResult(MapSource.SHAPEFILE);
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
     * Attempts to obtain overlapping district information for a specific district of arbitrary type.
     * @param districtType DistrictType the DistrictType of the district to get intersections with
     * @param districtId String the id of the district to get intersections with
     * @param intersectType DistrictType the type of district we are searching for intersections with districtId
     * @return DistrictResult with overlaps set.
     */
    public IntersectResult getIntersectionResult(DistrictType districtType, String districtId, DistrictType intersectType) {
        DistrictMap sourceMap = sqlShapefileDao.getDistrictMap(districtType, districtId);
        // We only need the overlap for the specified intersect type
        DistrictOverlap overlap = sqlShapefileDao.getDistrictOverlap(intersectType, districtType, districtId);
        return new IntersectResult(MapSource.SHAPEFILE, sourceMap, overlap);
    }
}
