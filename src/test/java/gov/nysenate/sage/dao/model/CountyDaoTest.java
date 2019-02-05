package gov.nysenate.sage.dao.model;

import gov.nysenate.sage.BaseTests;
import gov.nysenate.sage.annotation.IntegrationTest;
import gov.nysenate.sage.config.DatabaseConfig;
import gov.nysenate.sage.dao.model.county.SqlCountyDao;
import gov.nysenate.sage.model.district.County;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class CountyDaoTest extends BaseTests {

    @Autowired
    SqlCountyDao sqlCountyDao;
    private static int TOTAL_NUMBER_OF_COUNTIES = 62;

    @Test
    @Transactional(value = DatabaseConfig.geoApiTxManager)
    public void getAllCountiesTest()
    {
        List<County> counties = sqlCountyDao.getCounties();
        assertNotNull(counties);
        assertEquals(TOTAL_NUMBER_OF_COUNTIES, counties.size());

        County county = counties.get(0);

        assertNotEquals(county.getId(), 0);
        assertNotNull(county.getName());
        assertNotEquals(county.getFipsCode(), 0);
    }

    @Test
    @Transactional(value = DatabaseConfig.geoApiTxManager)
    public void getCountyByIdTest()
    {
        County county = sqlCountyDao.getCountyById(14);
        assertEquals("erie", county.getName().toLowerCase());
    }

    @Test
    @Transactional(value = DatabaseConfig.geoApiTxManager)
    public void getCountyByNameTest()
    {
        County county = sqlCountyDao.getCountyByName("Erie");
        assertEquals("erie", county.getName().toLowerCase());

        county = sqlCountyDao.getCountyByName("erie");
        assertEquals("erie", county.getName().toLowerCase());
    }

    @Test
    @Transactional(value = DatabaseConfig.geoApiTxManager)
    public void getCountyByFipsCodeTest()
    {
        County county = sqlCountyDao.getCountyByFipsCode(29);
        assertEquals("erie", county.getName().toLowerCase());
    }

    @Test
    @Transactional(value = DatabaseConfig.geoApiTxManager)
    public void getFipsCountyMapTest()
    {
        Map<Integer, County> fipsCountyMap = sqlCountyDao.getFipsCountyMap();
        assertEquals(fipsCountyMap.get(1).getName().toLowerCase(), "albany");
        assertEquals(fipsCountyMap.get(59).getName().toLowerCase(), "nassau");
    }
}
