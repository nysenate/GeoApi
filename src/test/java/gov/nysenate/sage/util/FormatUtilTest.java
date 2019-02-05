package gov.nysenate.sage.util;

import gov.nysenate.sage.BaseTests;
import gov.nysenate.sage.annotation.IntegrationTest;
import gov.nysenate.sage.config.DatabaseConfig;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertEquals;

@Category(IntegrationTest.class)
public class FormatUtilTest extends BaseTests {

    @Test
    @Transactional(value = DatabaseConfig.geoApiTxManager)
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
