package gov.nysenate.sage.util;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class FormatUtilTest
{
    @Test
    public void toCamelCaseTest()
    {
        String s = "";
        assertEquals("", FormatUtil.toCamelCase(s));
        s = null;
        assertEquals(null, FormatUtil.toCamelCase(s));
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
