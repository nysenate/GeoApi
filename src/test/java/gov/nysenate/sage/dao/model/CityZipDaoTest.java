package gov.nysenate.sage.dao.model;

import gov.nysenate.sage.TestBase;
import gov.nysenate.sage.dao.provider.CityZipDBDao;
import gov.nysenate.sage.util.FormatUtil;
import org.junit.Before;
import org.junit.Test;

public class CityZipDaoTest extends TestBase {

    private CityZipDBDao czDao;

    @Before
    public void setUp(){
        czDao = new CityZipDBDao();
    }

    @Test
    public void testGetCityByZip() throws Exception {

    }

    @Test
    public void testGetZipsByCity() throws Exception {
        FormatUtil.printObject(czDao.getZipsByCity("troy"));
    }
}
