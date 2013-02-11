package gov.nysenate.sage.service;

import gov.nysenate.sage.TestBase;
import gov.nysenate.sage.adapter.USPS;
import gov.nysenate.sage.model.Address;
import gov.nysenate.sage.model.AddressResult;

import gov.nysenate.sage.util.FormatUtil;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * Test the functionality of the USPS adapter by executing a few sample requests and
 * comparing them to the expected responses.
 */
public class USPS_Test extends TestBase
{
    private Logger logger = Logger.getLogger(USPS_Test.class);
    private USPS usps;

    @Before
    public void setUp()
    {
        try {
            this.usps = new USPS();
        }
        catch(Exception ex){
            System.err.println(ex.getMessage());
        }
    }

    @Test
    public void USPS_SingleAddressValidate_ReturnsAddressResult()
    {
        Address address = new Address("44 Fairlawn Ave", "Apt 2B", "Albany", "NY", "12203", "");
        AddressResult addressResult = usps.validate(address);

        logger.debug(FormatUtil.toJsonString(addressResult));

        assertNotNull(addressResult);
        assertNotNull(addressResult.getAddress());
        assertEquals("44 FAIRLAWN AVE APT 2B", addressResult.getAddress().getAddr1());
        assertEquals("ALBANY", addressResult.getAddress().getCity());
        assertEquals("NY", addressResult.getAddress().getState());
        assertEquals("12203", addressResult.getAddress().getZip5());
        assertEquals("1914", addressResult.getAddress().getZip4());
    }

    @Test
    public void USPS_MultipleAddressValidate_ReturnsAddressResult()
    {
        ArrayList<Address> addresses = new ArrayList<>(Arrays.asList(
            new Address("71 14th Street", "", "Troy", "NY", "", ""),
            new Address("214 8th Street", "", "Troy", "NY", "12180", ""),
            new Address("101 East State Street", "", "Olean", "NY", "14760", ""),
            new Address("Oak Hill Park", "", "Olean", "NY", "14760", ""), /** This one will be invalid */
            new Address("2012 E Rivr Road", "", "Olean", "NY", "14760", ""),
            new Address("44 Fairlawn Ave", "Apt 2B", "Albany", "NY", "12203", "")
        ));

        ArrayList<AddressResult> addressResults = usps.validate(addresses);

        logger.debug(FormatUtil.toJsonString(addressResults));

        assertEquals(6, addressResults.size());
        assertEquals(USPS.class.getSimpleName(), addressResults.get(0).getSource());

        Address expected = new Address("71 14TH ST", "", "TROY", "NY", "12180", "4209");
        assertTrue(addressResults.get(0).isValidated());
        assertAddressEquals(expected, addressResults.get(0).getAddress());

        expected = new Address("214 8TH ST", "", "TROY", "NY", "12180", "2931");
        assertTrue(addressResults.get(1).isValidated());
        assertAddressEquals(expected, addressResults.get(1).getAddress());

        expected = new Address("101 E STATE ST", "", "OLEAN", "NY", "14760", "2776");
        assertTrue(addressResults.get(2).isValidated());
        assertAddressEquals(expected, addressResults.get(2).getAddress());

        assertFalse(addressResults.get(3).isValidated());
        assertNull(addressResults.get(3).getAddress());

        expected = new Address("2012 E RIVER RD", "", "OLEAN", "NY", "14760", "9309");
        assertTrue(addressResults.get(4).isValidated());
        assertAddressEquals(expected, addressResults.get(4).getAddress());

        expected = new Address("44 FAIRLAWN AVE APT 2B", "", "ALBANY", "NY", "12203", "1914");
        assertTrue(addressResults.get(5).isValidated());
        assertAddressEquals(expected, addressResults.get(5).getAddress());
    }

    @Test
    public void USPS_CityStateLookup_ReturnsAddressResult()
    {
        ArrayList<Address> addresses = new ArrayList<>(Arrays.asList(
                new Address("71 14th Street", "", "", "", "12180", ""),
                new Address("214 8th Street", "", "", "", "12180", ""),
                new Address("101 East State Street", "", "", "NY", "14760", ""),
                new Address("", "", "", "", "99999", ""), /** This one will be invalid */
                new Address("2012 E Rivr Road", "", "Olean", "", "14760", ""),
                new Address("44 Fairlawn Ave", "Apt 2B", "", "NY", "12203", "")
        ));

        ArrayList<AddressResult> addressResults = usps.lookupCityState(addresses);
        assertEquals(6, addressResults.size());
        assertEquals(USPS.class.getSimpleName(), addressResults.get(0).getSource());

        logger.debug(FormatUtil.toJsonString(addressResults));

        Address expected = new Address("71 14TH ST", "", "TROY", "NY", "12180", "4209");
        assertTrue(addressResults.get(0).isValidated());
        assertCityStateEquals(expected, addressResults.get(0).getAddress());

        expected = new Address("214 8TH ST", "", "TROY", "NY", "12180", "2931");
        assertTrue(addressResults.get(1).isValidated());
        assertCityStateEquals(expected, addressResults.get(1).getAddress());

        expected = new Address("101 E STATE ST", "", "OLEAN", "NY", "14760", "2776");
        assertTrue(addressResults.get(2).isValidated());
        assertCityStateEquals(expected, addressResults.get(2).getAddress());

        assertFalse(addressResults.get(3).isValidated());

        expected = new Address("2012 E RIVER RD", "", "OLEAN", "NY", "14760", "9309");
        assertTrue(addressResults.get(4).isValidated());
        assertCityStateEquals(expected, addressResults.get(4).getAddress());

        expected = new Address("44 FAIRLAWN AVE APT 2B", "", "ALBANY", "NY", "12203", "1914");
        assertTrue(addressResults.get(5).isValidated());
        assertCityStateEquals(expected, addressResults.get(5).getAddress());
    }

    private void assertAddressEquals(Address expected, Address actual)
    {
        assertEquals(expected.getAddr1(), actual.getAddr1());
        assertEquals(expected.getAddr2(), actual.getAddr2());
        assertEquals(expected.getCity(), actual.getCity());
        assertEquals(expected.getState(), actual.getState());
        assertEquals(expected.getZip5(), actual.getZip5());
        assertEquals(expected.getZip4(), actual.getZip4());
    }

    private void assertCityStateEquals(Address expected, Address actual)
    {
        assertEquals(expected.getCity(), actual.getCity());
        assertEquals(expected.getState(), actual.getState());
    }
}
