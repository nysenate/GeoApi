package gov.nysenate.sage.dao.model;

import gov.nysenate.sage.TestBase;
import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.util.FormatUtil;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class DistrictMapDaoTest extends TestBase
{
    @Test
    public void getStateDistrictMapTest()
    {
        DistrictMap senateMap = DistrictMapDao.getStateDistrictMap(DistrictType.SENATE, "44");
        assertNotNull(senateMap);
        assertNotNull(senateMap.getPolygon());
        FormatUtil.printObject(senateMap);
    }

    @Test
    public void getStateDistrictMapTest_OnlyLoadsOnce()
    {
        /** View the log response here to ensure it calls the dao once */
        DistrictMap senateMap = DistrictMapDao.getStateDistrictMap(DistrictType.SENATE, "44");
        DistrictMap congressionalMap = DistrictMapDao.getStateDistrictMap(DistrictType.CONGRESSIONAL, "44");
    }

    @Test
    public void getStateDistrictMapTest_UnmappedDistrictTypeReturnsNull()
    {
        DistrictMap electionMap = DistrictMapDao.getStateDistrictMap(DistrictType.ELECTION, "2");
        assertNull(electionMap);
    }

    @Test
    public void getStateDistrictMapTest_UnmappedCodeReturnsNull()
    {
        DistrictMap senateMap = DistrictMapDao.getStateDistrictMap(DistrictType.SENATE, "-1");
        assertNull(senateMap);
    }
}
