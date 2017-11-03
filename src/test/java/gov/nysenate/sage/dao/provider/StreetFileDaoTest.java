package gov.nysenate.sage.dao.provider;

import gov.nysenate.sage.TestBase;
import gov.nysenate.sage.model.address.DistrictedStreetRange;
import gov.nysenate.sage.model.address.StreetAddress;
import gov.nysenate.sage.util.FormatUtil;
import gov.nysenate.sage.util.StreetAddressParser;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertNotNull;

public class StreetFileDaoTest extends TestBase
{
    StreetFileDao streetFileDao;

    @Before
    public void setUp(){
        streetFileDao = new StreetFileDao();
    }

    @Test
    public void test() throws Exception
    {
        StreetAddress s = StreetAddressParser.parseAddress("143D Edgewater park,bronx, NY 10465");
        assertNotNull(streetFileDao.getDistAddressByHouse(s));
    }

    @Test
    public void getDistrictStreetRangesByZipTest()
    {
        List<DistrictedStreetRange> ranges = streetFileDao.getDistrictStreetRangesByZip("12180");
        assertNotNull(ranges);
    }

    @Test
    public void getAllStateDistrictMatchesTest()
    {
        assertNotNull(streetFileDao.getAllStandardDistrictMatches("WESTERN AVE", "12203"));
    }
}
