package gov.nysenate.sage.dao.model;

import gov.nysenate.sage.TestBase;
import gov.nysenate.sage.dao.model.AssemblyDao;
import gov.nysenate.sage.model.district.Assembly;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

public class AssemblyDaoTest extends TestBase
{
    private AssemblyDao assemblyDao;
    private static int TOTAL_ASSEMBLIES = 150;

    @Before
    public void setUp()
    {
        assemblyDao = new AssemblyDao();
    }

    @Test
    public void getAssembliesTest()
    {
        List<Assembly> assemblyList = assemblyDao.getAssemblies();
        assertNotNull(assemblyList);
        assertEquals(TOTAL_ASSEMBLIES, assemblyList.size());

        Assembly a = assemblyList.get(0);
        assertNotNull(a.getMemberName());
        assertNotNull(a.getMemberUrl());
        assertFalse(a.getMemberName().isEmpty());
        assertFalse(a.getMemberUrl().isEmpty());
        assertTrue(a.getDistrict() > 0);
    }

    /**
     * Data subject to change!
     */
    @Test
    public void getAssemblyByDistrictTest()
    {
        Assembly assembly = assemblyDao.getAssemblyByDistrict(6);
        assertEquals("Ramos, Phil", assembly.getMemberName());
    }

    @Test
    public void getAssemblyByInvalidDistrictTest()
    {
        Assembly assembly = assemblyDao.getAssemblyByDistrict(0);
        assertNull(assembly);
    }
}
