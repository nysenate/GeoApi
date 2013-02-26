package gov.nysenate.sage.dao;

import gov.nysenate.sage.TestBase;
import gov.nysenate.sage.model.district.County;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

public class CountyDaoTest extends TestBase
{
    CountyDao countyDao;
    private static int TOTAL_NUMBER_OF_COUNTIES = 62;

    @Before
    public void setUp() { countyDao = new CountyDao(); }

    @Test
    public void getAllCountiesTest()
    {
        List<County> counties = countyDao.getCounties();
        assertNotNull(counties);
        assertEquals(TOTAL_NUMBER_OF_COUNTIES, counties.size());

        County county = counties.get(0);

        assertNotEquals(county.getId(), 0);
        assertNotNull(county.getName());
        assertNotEquals(county.getFipsCode(), 0);
    }

    @Test
    public void getCountyByIdTest()
    {
        County county = countyDao.getCountyById(14);
        assertEquals("erie", county.getName().toLowerCase());
    }

    @Test
    public void getCountyByNameTest()
    {
        County county = countyDao.getCountyByName("Erie");
        assertEquals("erie", county.getName().toLowerCase());

        county = countyDao.getCountyByName("erie");
        assertEquals("erie", county.getName().toLowerCase());
    }

    @Test
    public void getCountyByFipsCodeTest()
    {
        County county = countyDao.getCountyByFipsCode(29);
        assertEquals("erie", county.getName().toLowerCase());
    }









}
