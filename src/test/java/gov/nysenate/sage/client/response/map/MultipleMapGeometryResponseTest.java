package gov.nysenate.sage.client.response.map;

import gov.nysenate.sage.annotation.UnitTest;
import gov.nysenate.sage.client.view.map.DistrictMapView;
import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.MapListResult;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;

@Category(UnitTest.class)
public class MultipleMapGeometryResponseTest {
    /** Numbered names share a prefix, so they must sort by the number, not lexicographically. */
    @Test
    public void numberedDistrictsSortByCodeTest() {
        assertOrder(DistrictType.SENATE,
                List.of("Senate District 2", "Senate District 10", "Senate District 1"),
                List.of("Senate District 1", "Senate District 2", "Senate District 10"));
    }

    @Test
    public void namedDistrictsSortAlphabeticallyTest() {
        assertOrder(DistrictType.COUNTY,
                List.of("Allegany County", "Cattaraugus County", "Albany County"),
                List.of("Albany County", "Allegany County", "Cattaraugus County"));
    }

    /** Towns and cities ignore the prefix. */
    @Test
    public void townsAndCitiesSortByBaseNameTest() {
        assertOrder(DistrictType.TOWN_CITY,
                List.of("City of Ithaca", "Town of Amity", "City of Albany"),
                List.of("City of Albany", "Town of Amity", "City of Ithaca"));
    }

    /** A town and a city can share a base name, and still need a stable relative order. */
    @Test
    public void sharedBaseNameSortsByFullNameTest() {
        assertOrder(DistrictType.TOWN_CITY,
                List.of("Town of Amsterdam", "City of Amsterdam"),
                List.of("City of Amsterdam", "Town of Amsterdam"));
    }

    /**
     * Builds a response holding a map per name, applies the names in the given order,
     * then asserts that {@link MultipleMapGeometryResponse#getDistricts()} returns them in the expected order.
     */
    private static void assertOrder(DistrictType type, List<String> namesToAdd, List<String> expectedOrder) {
        var maps = new LinkedHashMap<String, DistrictMap>();
        for (int i = 0; i < namesToAdd.size(); i++) {
            String code = String.valueOf(i);
            maps.put(code, new DistrictMap(type, code));
        }
        var response = new MultipleMapGeometryResponse(new MapListResult(maps));
        List<DistrictMapView> views = response.getMapViews();
        for (int i = 0; i < namesToAdd.size(); i++) {
            views.get(i).setName(namesToAdd.get(i));
        }
        assertEquals(expectedOrder, response.getDistricts().stream().map(DistrictMapView::getName).toList());
    }
}
