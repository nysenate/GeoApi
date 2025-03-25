package gov.nysenate.sage.model;

import gov.nysenate.sage.annotation.UnitTest;
import gov.nysenate.sage.model.district.DistrictType;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.HashSet;
import java.util.Set;

import static gov.nysenate.sage.model.district.DistrictType.*;
import static org.junit.Assert.assertEquals;

@Category(UnitTest.class)
public class DistrictTypeTest {
    @Test
    public void getStandardTypesTest() {
        assertEquals(Set.of(SENATE, CONGRESSIONAL, ZIP, ASSEMBLY, SCHOOL, TOWN_CITY, COUNTY),
                new HashSet<>(DistrictType.getStandardTypes()));
    }
}
