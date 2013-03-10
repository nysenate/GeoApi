package gov.nysenate.sage.dao;

import gov.nysenate.sage.TestBase;
import gov.nysenate.sage.dao.provider.StreetFileDao;
import gov.nysenate.sage.model.address.StreetAddress;
import gov.nysenate.sage.util.AddressParser;
import gov.nysenate.sage.util.FormatUtil;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;

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
        StreetAddress s = AddressParser.parseAddress("44 Fairlawn ave, Albany NY 12203");
        FormatUtil.printObject(s);
        FormatUtil.printObject(streetFileDao.getDistAddressByStreet(s));
    }


}
