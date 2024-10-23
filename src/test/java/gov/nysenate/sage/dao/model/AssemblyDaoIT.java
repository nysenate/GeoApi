package gov.nysenate.sage.dao.model;

import gov.nysenate.sage.BaseTests;
import gov.nysenate.sage.annotation.IntegrationTest;
import gov.nysenate.sage.config.DatabaseConfig;
import gov.nysenate.sage.dao.model.assembly.SqlAssemblyDao;
import gov.nysenate.sage.model.district.Assembly;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class AssemblyDaoIT extends BaseTests {

    @Autowired
    SqlAssemblyDao sqlAssemblyDao;

    @Test
    @Transactional(value = DatabaseConfig.geoApiTxManager)
    public void getAssembliesTest()
    {
        List<Assembly> assemblyList = sqlAssemblyDao.getAssemblies();
        assertNotNull(assemblyList);
        assertEquals(150, assemblyList.size());

        Assembly a = assemblyList.get(0);
        assertNotNull(a.getMemberName());
        assertNotNull(a.getMemberUrl());
        assertFalse(a.getMemberName().isEmpty());
        assertFalse(a.getMemberUrl().isEmpty());
        assertTrue(a.getDistrict() > 0);
    }

    @Test
    @Transactional(value = DatabaseConfig.geoApiTxManager)
    public void insertAndDeleteTest()
    {
        Assembly assembly = new Assembly(200,"TEST TEST","NY.GOV.ASSSEMBLY.TEST");
        sqlAssemblyDao.insertAssembly(assembly);
        sqlAssemblyDao.deleteAssemblies(200);
    }


    @Test
    @Transactional(value = DatabaseConfig.geoApiTxManager)
    public void getAssemblyByInvalidDistrictTest()
    {
        Assembly assembly = sqlAssemblyDao.getAssemblyByDistrict(0);
        assertNull(assembly);
    }
}
