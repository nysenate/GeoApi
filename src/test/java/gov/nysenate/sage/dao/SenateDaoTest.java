package gov.nysenate.sage.dao;

import gov.nysenate.sage.TestBase;
import gov.nysenate.services.model.Senator;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Collection;

public class SenateDaoTest extends TestBase
{
    private SenateDao senateDao;
    private static int NUMBER_OF_SENATORS = 63;

    @Before
    public void setUp()
    {
        this.senateDao = new SenateDao();
    }

    @Test
    public void getSenatorsTest()
    {
        Collection<Senator> senators = this.senateDao.getSenators();
        assertNotNull(senators);
        assertEquals(NUMBER_OF_SENATORS, senators.size());

        for (Senator senator : senators){
            assertNotNull(senator.getDistrict());
            assertNotNull(senator.getName());
        }
    }

    @Test
    public void getSenatorByDistrictTest()
    {
        Senator senator = this.senateDao.getSenatorByDistrict(7);
        assertTrue(senator.getName().contains("Martins"));
        assertEquals(7, senator.getDistrict().getNumber());
    }
}
