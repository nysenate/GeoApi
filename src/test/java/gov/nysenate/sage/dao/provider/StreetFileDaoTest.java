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

public class StreetFileDaoTest extends TestBase
{
    StreetFileDao streetFileDao;
    Connection connection;

    @Before
    public void setUp(){
        streetFileDao = new StreetFileDao();
        connection = streetFileDao.getTigerConnection();
    }

    @Test
    public void test() throws Exception
    {
        StreetAddress s = StreetAddressParser.parseAddress("143D Edgewater park,bronx, NY 10465");
        FormatUtil.printObject(s);
        FormatUtil.printObject(streetFileDao.getDistAddressByHouse(s));
    }

    @Test
    public void getDistrictStreetRangesByZipTest()
    {
        List<DistrictedStreetRange> ranges = streetFileDao.getDistrictStreetRangesByZip("12180");
        FormatUtil.printObject(ranges.size());
        FormatUtil.printObject(ranges);
    }

    @Test
    public void getAllStateDistrictMatchesTest()
    {
        FormatUtil.printObject(streetFileDao.getAllStandardDistrictMatches("WESTERN AVE", "12203"));
        //FormatUtil.printObject(StringEscapeUtils.escapeSql("anything' OR 'x'='x"));
    }
}
