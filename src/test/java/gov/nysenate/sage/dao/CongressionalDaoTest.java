package gov.nysenate.sage.dao;

import gov.nysenate.sage.TestBase;
import gov.nysenate.sage.model.district.Congressional;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class CongressionalDaoTest extends TestBase
{
    private CongressionalDao congressionalDao;
    private static int TOTAL_CONGRESSIONALS = 27;

    @Before
    public void setUp()
    {
        congressionalDao = new CongressionalDao();
    }

    @Test
    public void getCongressionalsTest()
    {
        List<Congressional> congressionalList = congressionalDao.getCongressionals();
        assertNotNull(congressionalList);
        assertEquals(TOTAL_CONGRESSIONALS, congressionalList.size());

        Congressional c = congressionalList.get(0);
        assertNotNull(c.getMemberName());
        assertNotNull(c.getMemberUrl());
        assertFalse(c.getMemberName().isEmpty());
        assertFalse(c.getMemberUrl().isEmpty());
        assertTrue(c.getDistrict() > 0);
    }

    /**
     * Data subject to change!
     */
    @Test
    public void getCongressionalByDistrictTest()
    {
        Congressional congressional = congressionalDao.getCongressionalByDistrict(6);
        assertEquals("Meng, Grace", congressional.getMemberName());
    }

    @Test
    public void getCongressionalByInvalidDistrictTest()
    {
        Congressional congressional = congressionalDao.getCongressionalByDistrict(0);
        assertNull(congressional);
    }
}
