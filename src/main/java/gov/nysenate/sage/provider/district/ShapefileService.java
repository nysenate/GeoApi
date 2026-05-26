package gov.nysenate.sage.provider.district;

import gov.nysenate.sage.dao.provider.shapefile.ShapefileDao;
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

/**
 * Used to return district maps for state level districts.
 */
@Service
public class ShapefileService {
    private final ShapefileDao shapefileDao;

    @Autowired
    public ShapefileService(ShapefileDao shapefileDao) {
        this.shapefileDao = shapefileDao;
    }

    /** Provides a district map given a specific district */
    public MapResult getDistrictMap(DistrictType districtType, String code) {
        if (code == null || code.isBlank()) {
            return new MapResult(ResultStatus.MISSING_DISTRICT_CODE);
        }
        DistrictMap map = shapefileDao.getDistrictMap(districtType, code);
        if (map == null) {
            return new MapResult(ResultStatus.NO_MAP_RESULT);
        }
        return new MapResult(map);
    }

    /** Provides a collection of all district maps for a given type */
    public MapListResult getDistrictMaps(DistrictType districtType) {
        SortedSet<DistrictMap> mapSet = shapefileDao.getDistrictMaps(districtType);
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
        DistrictMap sourceMap = shapefileDao.getDistrictMap(sourceType, sourceId);
        List<IntersectMap> overlaps = shapefileDao.getDistrictOverlap(sourceType, intersectWith, sourceId);
        return new IntersectResult(sourceMap, intersectWith, overlaps);
    }

    public List<DistrictType> getTypes() {
        return List.copyOf(shapefileDao.getTypes());
    }
}
