package gov.nysenate.sage.model;

import gov.nysenate.sage.annotation.UnitTest;
import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictType;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Category(UnitTest.class)
public class DistrictMapTest {
    @Test
    public void testCompareTo() {
        var map1 = new DistrictMap(DistrictType.SENATE, "District 1", "1");
        var map2 = new DistrictMap(DistrictType.SENATE, "District 2", "2");
        assertTrue(map1.compareTo(map2) < 0);
        map1 = new DistrictMap(DistrictType.SENATE, "District 10", "10");
        assertTrue(map1.compareTo(map2) > 0);
        map2 = new DistrictMap(DistrictType.SENATE, "District 10", "10");
        assertEquals(0, map1.compareTo(map2));

        map1 = new DistrictMap(DistrictType.COUNTY, "Albany", "1");
        map2 = new DistrictMap(DistrictType.COUNTY, "Allegany", "3");
        assertTrue(map1.compareTo(map2) < 0);

        map1 = new DistrictMap(DistrictType.TOWN_CITY, "Town of Amity", "02011");
        map1.setBaseName("Amity");
        map2 = new DistrictMap(DistrictType.TOWN_CITY, "Town of Amsterdam", "02077");
        map2.setBaseName("Amsterdam");
        assertTrue(map1.compareTo(map2) < 0);
        map1 = new DistrictMap(DistrictType.TOWN_CITY, "City of Amsterdam", "02066");
        map1.setBaseName("Amsterdam");
        assertTrue(map1.compareTo(map2) < 0);
    }
}
