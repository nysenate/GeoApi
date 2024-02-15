package gov.nysenate.sage.util;

import gov.nysenate.sage.annotation.UnitTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@Category(UnitTest.class)
public class FormatUtilTest {

    @Test
    public void toCamelCaseTest()
    {
        String s = "";
        assertEquals("", FormatUtil.toCamelCase(s));
        s = null;
        assertNull(FormatUtil.toCamelCase(s));
        s = "street_address_1";
        assertEquals("streetAddress1", FormatUtil.toCamelCase(s));
        s = "alreadyCamelCased";
        assertEquals("alreadyCamelCased", FormatUtil.toCamelCase(s));
        s = "multiple__underscores";
        assertEquals("multipleUnderscores", FormatUtil.toCamelCase(s));
        s = "Already_Capitalized";
        assertEquals("alreadyCapitalized", FormatUtil.toCamelCase(s));

        s = "state_province_id";
        assertEquals("stateProvinceId", FormatUtil.toCamelCase(s));

        s = "ny_senate_district_47";
        assertEquals("nySenateDistrict47", FormatUtil.toCamelCase(s));
    }
}
