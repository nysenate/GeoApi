package gov.nysenate.sage.model.district;

import gov.nysenate.sage.TestBase;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static gov.nysenate.sage.model.district.DistrictType.*;
import static org.junit.Assert.*;

public class DistrictTypeTest extends TestBase
{
    @Test
    public void getStandardTypesTest()
    {
        List<DistrictType> standardTypes = DistrictType.getStandardTypes();
        List<DistrictType> expectedTypes = Arrays.asList(SENATE, CONGRESSIONAL, ASSEMBLY, SCHOOL, TOWN, COUNTY, ELECTION);
        assertEquals(new HashSet<>(standardTypes), new HashSet<>(expectedTypes));
    }

    @Test
    public void getExtendedTypesTest()
    {
        List<DistrictType> extendedTypes = DistrictType.getExtendedTypes();
        List<DistrictType> expectedTypes = Arrays.asList(FIRE, WARD, CITY, CLEG, VILLAGE);
        assertEquals(new HashSet<>(extendedTypes), new HashSet<>(expectedTypes));
    }

    @Test
    public void getAllTypesTest()
    {
        List<DistrictType> allTypes = DistrictType.getAllTypes();
        assertEquals(new HashSet<>(allTypes), new HashSet<>(Arrays.asList(DistrictType.values())));
    }

    @Test
    public void resolveType()
    {
        DistrictType resolvedSenate = DistrictType.resolveType("senate");
        DistrictType resolvedAssembly = DistrictType.resolveType("AsSemblY");
        DistrictType resolvedCongressional = DistrictType.resolveType("CONGRESSIONAL");
        DistrictType resolvedNull = DistrictType.resolveType("Something");

        assertEquals(SENATE,resolvedSenate);
        assertEquals(ASSEMBLY,resolvedAssembly);
        assertEquals(CONGRESSIONAL,resolvedCongressional);
        assertNull(resolvedNull);
    }





}
