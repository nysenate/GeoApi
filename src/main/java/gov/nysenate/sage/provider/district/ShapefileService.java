package gov.nysenate.sage.provider.district;

import gov.nysenate.sage.dao.provider.district.SqlShapefileDao;
import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.district.IntersectMap;
import gov.nysenate.sage.model.result.IntersectResult;
import gov.nysenate.sage.model.result.MapListResult;
import gov.nysenate.sage.model.result.MapResult;
import gov.nysenate.sage.model.result.ResultStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.SortedSet;

@Service
public class ShapefileService implements MapService {
    private final SqlShapefileDao sqlShapefileDao;

    @Autowired
    public ShapefileService(SqlShapefileDao sqlShapefileDao) {
        this.sqlShapefileDao = sqlShapefileDao;
    }

    /** {@inheritDoc} */
    @Override
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

    /** {@inheritDoc} */
    @Override
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
}
