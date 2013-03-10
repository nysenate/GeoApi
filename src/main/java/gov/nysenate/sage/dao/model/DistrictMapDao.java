package gov.nysenate.sage.dao.model;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.provider.DistrictShapefileDao;
import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictType;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * Holds a memory cached map of the district boundaries for each state district code.
 * The state districts are identified by the districts associated with
 * <code>DistrictType.getStateBasedTypes()</code>
 *
 * The reason that the other district types cannot be cached in this manner is because
 * those districts either require an additional identifier such as a county or town code
 * to be unique or return multiple polygons for a given district code.
 */
public class DistrictMapDao extends BaseDao
{
    private static Logger logger = Logger.getLogger(DistrictMapDao.class);
    private static DistrictShapefileDao shapefileDao = new DistrictShapefileDao();
    private static Map<DistrictType, Map<String, DistrictMap>> districtMaps;

    public static DistrictMap getStateDistrictMap(DistrictType districtType, String code)
    {
        if (getStateDistrictMaps() != null && getStateDistrictMaps().containsKey(districtType)) {
            return getStateDistrictMaps().get(districtType).get(code);
        }
        else {
            logger.error("District Maps for " + districtType + " have not been set.");
            return null;
        }
    }

    public static Map<DistrictType, Map<String, DistrictMap>> getStateDistrictMaps()
    {
        if (districtMaps == null){
            logger.debug("Retrieving state district maps from shapefile dao..");
            districtMaps = shapefileDao.getDistrictMaps(DistrictType.getStateBasedTypes());
        }
        return districtMaps;
    }
}
