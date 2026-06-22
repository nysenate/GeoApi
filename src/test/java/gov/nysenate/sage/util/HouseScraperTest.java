package gov.nysenate.sage.util;

import gov.nysenate.sage.annotation.UnitTest;
import gov.nysenate.sage.model.district.DistrictMember;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertFalse;

@Category(UnitTest.class)
public class HouseScraperTest {
    @Test
    public void testScraper() throws IOException {
        Map<Long, DistrictMember> memberMap = HouseScraper.getHouseMembers();
        assertFalse(memberMap.isEmpty());
    }
}
