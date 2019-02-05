package gov.nysenate.sage.dao.model;

import gov.nysenate.sage.BaseTests;
import gov.nysenate.sage.annotation.IntegrationTest;
import gov.nysenate.sage.config.DatabaseConfig;
import gov.nysenate.sage.dao.model.congressional.SqlCongressionalDao;
import gov.nysenate.sage.model.district.Congressional;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNull;

@Category(IntegrationTest.class)
public class CongressionalDaoTest extends BaseTests {

    @Autowired
    SqlCongressionalDao sqlCongressionalDao;

    private static int TOTAL_CONGRESSIONALS = 27;

    @Test
    @Transactional(value = DatabaseConfig.geoApiTxManager)
    public void getCongressionalsTest()
    {
        List<Congressional> congressionalList = sqlCongressionalDao.getCongressionals();
        assertNotNull(congressionalList);
        assertEquals(TOTAL_CONGRESSIONALS, congressionalList.size());

        Congressional c = congressionalList.get(0);
        assertNotNull(c.getMemberName());
        assertNotNull(c.getMemberUrl());
        assertFalse(c.getMemberName().isEmpty());
        assertFalse(c.getMemberUrl().isEmpty());
        assertTrue(c.getDistrict() > 0);
    }

    @Test
    @Transactional(value = DatabaseConfig.geoApiTxManager)
    public void insertAndDeleteTest()
    {
        Congressional congressional =
                new Congressional(200,"TEST TEST","NY.GOV.CONGRESS.TEST");
        sqlCongressionalDao.insertCongressional(congressional);
        sqlCongressionalDao.deleteCongressional(200);
    }


    @Test
    @Transactional(value = DatabaseConfig.geoApiTxManager)
    public void getAssemblyByInvalidDistrictTest()
    {
        Congressional congressional = sqlCongressionalDao.getCongressionalByDistrict(0);
        assertNull(congressional);
    }
}

