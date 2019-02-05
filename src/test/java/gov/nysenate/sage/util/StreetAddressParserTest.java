package gov.nysenate.sage.util;

import gov.nysenate.sage.BaseTests;
import gov.nysenate.sage.annotation.IntegrationTest;
import gov.nysenate.sage.config.DatabaseConfig;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.StreetAddress;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.transaction.annotation.Transactional;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

@Category(IntegrationTest.class)
public class StreetAddressParserTest extends BaseTests {

    @Test
    @Transactional(value = DatabaseConfig.geoApiTxManager)
    public void edgeCases()
    {
        StreetAddress streetAddress = StreetAddressParser.parseAddress("Queens NY");
        assertNotNull(streetAddress);
        assertEquals("queens", streetAddress.getLocation().toLowerCase());
    }

    @Test
    @Transactional(value = DatabaseConfig.geoApiTxManager)
    public void semiParsedTest()
    {
        // PreDir with Internal
        Address address = new Address("21st Street");
        StreetAddress sa = new StreetAddress();
        StreetAddressParser.extractStreet(address.toString(), sa);
        StreetAddressParser.normalizeStreetAddress(sa);
    }

    @Test
    @Transactional(value = DatabaseConfig.geoApiTxManager)
    public void comparisonTest() {
        Address address = new Address("60 Jordan Road, Climax, NY");
        StreetAddress one = StreetAddressParser.parseAddress(address);
        StreetAddress two = StreetAddressParser.parseAddress(address);
        assertStreetAddressesAreEqual(one, two);
    }

    public void assertStreetAddressesAreEqual(StreetAddress s1, StreetAddress s2)
    {
        assertThat(s1, notNullValue());
        assertThat(s2, notNullValue());
        assertThat("bldgNum", s1.getBldgNum(), is(s2.getBldgNum()));
        assertThat("bldgChr", s1.getBldgChar(), is(s2.getBldgChar()));
        assertThat("preDir", s1.getPreDir(), is(s2.getPreDir()));
        assertThat("streetName", s1.getStreetName(), is(s2.getStreetName()));
        assertThat("streetType", s1.getStreetType(), is(s2.getStreetType()));
        assertThat("internal", s1.getInternal(), is(s2.getInternal()));
        assertThat("location", s1.getLocation(), is(s2.getLocation()));
        assertThat("postDir", s1.getPostDir(), is(s2.getPostDir()));
        assertThat("zip5", s1.getZip5(), is(s2.getZip5()));
        assertThat("zip4", s1.getZip4(), is(s2.getZip4()));
    }
}
