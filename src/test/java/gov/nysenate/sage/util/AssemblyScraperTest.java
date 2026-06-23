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
        test("John", "Smith", "John Smith", email);
        test("John", "Smith Jr.", "John Smith Jr.", email);
        test("John E.", "Smith", "John E. Smith", email);
        test("John Middle", "Smith", "John Middle Smith", email);
        email = "middleJ@nyassembly.gov";
        test("John", "Middle Smith", "John Middle Smith", email);
        test("John", "Middle-Smith", "John Middle-Smith", email);
        test("Tommy John ", "Schiavoni", "Tommy John Schiavoni", "schiavonitj@nyassembly.gov");
    }

    private static void test(String nameStart, String nameEnd, String baseName, String email) {
        Pair<String> expectedNameParts = new Pair<>(nameStart, nameEnd);
        assertEquals(expectedNameParts, AssemblyScraper.getNameParts(baseName, email));
    }
}
