package gov.nysenate.sage.util;

import gov.nysenate.sage.annotation.UnitTest;
import gov.nysenate.sage.model.district.DistrictMember;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.*;

@Category(UnitTest.class)
public class AssemblyScraperTest {
    @Test
    public void testScraper() throws IOException {
        Map<Long, DistrictMember> memberMap = AssemblyScraper.getAssemblyMembers();
        assertFalse(memberMap.isEmpty());
    }

    @Test
    public void testNameInversion() {
        String email = "smithJ@nyassembly.gov";
        test("Smith, John", "John Smith", email);
        test("Smith Jr., John", "John Smith Jr.", email);
        test("Smith, John E.", "John E. Smith", email);
        test("Smith, John Middle", "John Middle Smith", email);
        email = "middleJ@nyassembly.gov";
        test("Middle Smith, John", "John Middle Smith", email);
        test("Middle-Smith, John", "John Middle-Smith", email);
        test("Schiavoni, Tommy John", "Tommy John Schiavoni", "schiavonitj@nyassembly.gov");
    }

    private static void test(String expectedInvertedName, String baseName, String email) {
        assertEquals(expectedInvertedName, AssemblyScraper.getInvertedName(baseName, email));
    }
}
