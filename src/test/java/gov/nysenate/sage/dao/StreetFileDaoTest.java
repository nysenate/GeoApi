package gov.nysenate.sage.dao;

import gov.nysenate.sage.TestBase;
import gov.nysenate.sage.dao.provider.StreetFileDao;
import gov.nysenate.sage.model.address.DistrictedStreetRange;
import gov.nysenate.sage.model.address.StreetAddress;
import gov.nysenate.sage.util.AddressParser;
import gov.nysenate.sage.util.FormatUtil;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
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
        StreetAddress s = AddressParser.parseAddress("300 CENTRAL PARK W, APT 7K, New York, NY 10024");
        FormatUtil.printObject(s);
        FormatUtil.printObject(streetFileDao.getDistAddressByHouse(s));
    }

    @Test
    public void getDistrictStreetRangesByZipTest()
    {
        List<DistrictedStreetRange> ranges = streetFileDao.getDistrictStreetRangesByZip(12180);
        FormatUtil.printObject(ranges.size());
        FormatUtil.printObject(ranges);
    }



}
