package gov.nysenate.sage.model;

import gov.nysenate.sage.annotation.UnitTest;
import gov.nysenate.sage.model.district.DistrictType;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static gov.nysenate.sage.model.district.DistrictType.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@Category(UnitTest.class)
public class DistrictTypeTest {

    @Test
    public void getStandardTypesTest()
    {
        List<DistrictType> standardTypes = DistrictType.getStandardTypes();
        List<DistrictType> expectedTypes = Arrays.asList(SENATE, CONGRESSIONAL,ZIP, ASSEMBLY, SCHOOL, TOWN, COUNTY);
        assertEquals(new HashSet<>(expectedTypes), new HashSet<>(standardTypes));
    }

    @Test
    public void getExtendedTypesTest()
    {
        List<DistrictType> extendedTypes = DistrictType.getExtendedTypes();
        List<DistrictType> expectedTypes = Arrays.asList(FIRE, WARD, CITY, CLEG, VILLAGE, ELECTION, CITY_COUNCIL);
        assertEquals(new HashSet<>(expectedTypes), new HashSet<>(extendedTypes));
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
