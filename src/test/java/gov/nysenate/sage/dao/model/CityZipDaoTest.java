package gov.nysenate.sage.dao.model;

import gov.nysenate.sage.TestBase;
import gov.nysenate.sage.dao.provider.CityZipDBDao;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class CityZipDaoTest extends TestBase {

    private CityZipDBDao czDao;

    @Before
    public void setUp(){
        czDao = new CityZipDBDao();
    }

    @Test
    public void testGetZipsByCity() throws Exception {
        assertNotNull( czDao.getZipsByCity("troy"));
    }
}
