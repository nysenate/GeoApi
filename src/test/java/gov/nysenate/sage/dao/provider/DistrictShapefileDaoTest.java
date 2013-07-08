package gov.nysenate.sage.dao.provider;

import gov.nysenate.sage.TestBase;
import gov.nysenate.sage.dao.provider.DistrictShapefileDao;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.util.FormatUtil;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class DistrictShapefileDaoTest extends TestBase
{
    Logger logger = Logger.getLogger(DistrictShapefileDaoTest.class);
    DistrictShapefileDao dsDao;

    @Before
    public void setUp()
    {
        dsDao = new DistrictShapefileDao();
    }

    @Test
    public void getDistrictMapsTest()
    {
        Map<DistrictType, Map<String, DistrictMap>> distMaps = dsDao.getDistrictMapLookup();
        assertTrue(distMaps.containsKey(DistrictType.SENATE));
        assertTrue(distMaps.get(DistrictType.SENATE).containsKey("44"));
    }

    @Test
    public void getDistrictInfoFromPoint()
    {
        DistrictInfo dinfo = (dsDao.getDistrictInfo(new Point(42.74117729798573, -73.66938646729645), DistrictType.getStandardTypes(), true));
        FormatUtil.printObject(dinfo.getDistMap(DistrictType.ASSEMBLY));
    }

    @Test
    public void getNearbyDistrictsTest()
    {
        Map<String, DistrictMap> neighbors = dsDao.getNearbyDistricts(DistrictType.SENATE, new Point(40.714920, -73.795158), true, 2);
        assertTrue(neighbors.containsKey("14"));
        FormatUtil.printObject(neighbors);
    }

    @Test
    public void setsDistrictProximityTest()
    {
        FormatUtil.printObject(DistrictType.getStandardTypes());
        DistrictInfo dinfo = (dsDao.getDistrictInfo(new Point(40.712681, -73.797050), DistrictType.getStandardTypes(), false));
        FormatUtil.printObject(dinfo.getDistProximity(DistrictType.ZIP));

    }

    @Test
    public void getDistrictOverlapTest()
    {
        logger.info("Start");
        dsDao.getDistrictOverlap(DistrictType.SENATE, new HashSet<String>(Arrays.asList("44", "46")),
                                                        DistrictType.ZIP, new HashSet<String>(Arrays.asList("12203", "12205")));
        logger.info("End");

    }

    @Test
    public void getOverlapReferenceBoundaryTest()
    {
        logger.info("start");
        DistrictMap map = dsDao.getOverlapReferenceBoundary(DistrictType.ZIP, new HashSet<String>(Arrays.asList("12203", "12205")));
        FormatUtil.printObject(map);
        logger.info("end");
    }

    @Test
    public void getIntersectingStreetLineTest()
    {
        String jsonGeom = "{\"type\":\"MultiLineString\",\"coordinates\":[[[-73.799332,40.723235],[-73.798728,40.721816],[-73.798099,40.720345]],[[-73.796475,40.717592],[-73.795825,40.716267],[-73.794736,40.714057],[-73.793903,40.71237],[-73.793038,40.710711],[-73.792621,40.709997],[-73.792166,40.709218],[-73.791674,40.708379],[-73.791292,40.707725],[-73.790796,40.70687]],[[-73.789193,40.703204],[-73.788597,40.702103],[-73.788157,40.701268],[-73.787816,40.700544],[-73.787599,40.699866]],[[-73.783933,40.695494],[-73.783428,40.694702],[-73.782836,40.693897],[-73.781687,40.692315],[-73.780782,40.69103],[-73.779773,40.689281],[-73.778805,40.687623],[-73.77785,40.685989],[-73.777049,40.684622]],[[-73.771178,40.672531],[-73.770997,40.67091],[-73.770818,40.669016],[-73.770932,40.66834]]]}";
        FormatUtil.printObject(dsDao.getIntersectingStreetLine(DistrictType.SENATE, new HashSet<String>(Arrays.asList("11", "14", "15")), jsonGeom));
    }
}
