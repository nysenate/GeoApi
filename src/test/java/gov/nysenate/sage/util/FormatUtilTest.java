package gov.nysenate.sage.util;

import org.junit.Test;
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
    }
}
