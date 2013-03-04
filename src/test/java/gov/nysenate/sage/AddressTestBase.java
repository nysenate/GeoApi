package gov.nysenate.sage;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.result.AddressResult;
import gov.nysenate.sage.service.address.AddressService;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Common functions for testing address service implementations
 */
public abstract class AddressTestBase
{
    public static void assertSingleAddressValidation(AddressService addressService)
    {
        Address address = new Address("44 Fairlawn", "", "Albany", "ny", "12203", "");
        AddressResult addressResult = addressService.validate(address);

        assertNotNull(addressResult);
        assertNotNull(addressResult.getAddress());
        assertEquals("44 FAIRLAWN AVE", addressResult.getAddress().getAddr1().toUpperCase());
        assertEquals("ALBANY", addressResult.getAddress().getCity().toUpperCase());
        assertEquals("NY", addressResult.getAddress().getState().toUpperCase());
        assertEquals("12203", addressResult.getAddress().getZip5().toUpperCase());
        assertEquals("1914", addressResult.getAddress().getZip4().toUpperCase());
    }

    public static void assertMultiAddressValidation(AddressService addressService)
    {
        ArrayList<Address> addresses = new ArrayList<>(Arrays.asList(
                new Address("71 14th Street", "", "Troy", "NY", "", ""),
                new Address("214 8th Street", "", "Troy", "NY", "12180", ""),
                new Address("101 East State Street", "", "Olean", "NY", "14760", ""),
                new Address("706 washington", "", "Olean", "NY", "14760", ""),
                new Address("2012 E Rivr Road", "", "Olean", "NY", "14760", ""),
                new Address("44 Fairlawn Ave", "Apt 2B", "Albany", "NY", "12203", ""),
                new Address("", "", "", "", "18542", "")));

        ArrayList<AddressResult> addressResults = addressService.validate(addresses);

        assertEquals(addressService.getClass().getSimpleName(), addressResults.get(0).getSource());
        Address expected = new Address("71 14TH ST", "", "TROY", "NY", "12180", "4209");
        assertTrue(addressResults.get(0).isValidated());
        assertAddressEquals(expected, addressResults.get(0).getAddress());

        expected = new Address("214 8TH ST", "", "TROY", "NY", "12180", "2931");
        assertTrue(addressResults.get(1).isValidated());
        assertAddressEquals(expected, addressResults.get(1).getAddress());

        expected = new Address("101 E STATE ST", "", "OLEAN", "NY", "14760", "2776");
        assertTrue(addressResults.get(2).isValidated());
        assertAddressEquals(expected, addressResults.get(2).getAddress());

        expected = new Address("706 WASHINGTON ST", "", "OLEAN", "NY", "14760", "2316");
        assertTrue(addressResults.get(2).isValidated());
        assertAddressEquals(expected, addressResults.get(3).getAddress());

        expected = new Address("2012 E RIVER RD", "", "OLEAN", "NY", "14760", "9309");
        assertTrue(addressResults.get(4).isValidated());
        assertAddressEquals(expected, addressResults.get(4).getAddress());

        expected = new Address("44 FAIRLAWN AVE APT 2B", "", "ALBANY", "NY", "12203", "1914");
        assertTrue(addressResults.get(5).isValidated());
        assertAddressEquals(expected, addressResults.get(5).getAddress());

        assertFalse(addressResults.get(6).isValidated());
    }

    public static void assertCityStateLookup(AddressService addressService)
    {
        ArrayList<Address> addresses = new ArrayList<>(Arrays.asList(
                new Address("71 14th Street", "", "", "", "12180", ""),
                new Address("214 8th Street", "", "", "", "12180", ""),
                new Address("101 East State Street", "", "", "NY", "14760", ""),
                new Address("", "", "", "", "99999", ""), /** This one will be invalid */
                new Address("2012 E Rivr Road", "", "Olean", "", "14760", ""),
                new Address("44 Fairlawn Ave", "Apt 2B", "", "NY", "12203", "")
        ));

        ArrayList<AddressResult> addressResults = addressService.lookupCityState(addresses);
        assertEquals(6, addressResults.size());
        assertEquals(addressService.getClass().getSimpleName(), addressResults.get(0).getSource());

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

    public static void assertAddressEquals(Address expected, Address actual)
    {
        assertEquals(expected.getAddr1().toUpperCase(), actual.getAddr1().toUpperCase());
        assertEquals(expected.getAddr2().toUpperCase(), actual.getAddr2().toUpperCase());
        assertEquals(expected.getCity().toUpperCase(), actual.getCity().toUpperCase());
        assertEquals(expected.getState().toUpperCase(), actual.getState().toUpperCase());
        assertEquals(expected.getZip5().toUpperCase(), actual.getZip5().toUpperCase());
        assertEquals(expected.getZip4().toUpperCase(), actual.getZip4().toUpperCase());
    }

    public static void assertCityStateEquals(Address expected, Address actual)
    {
        assertEquals(expected.getCity().toUpperCase(), actual.getCity().toUpperCase());
        assertEquals(expected.getState().toUpperCase(), actual.getState().toUpperCase());
    }

    public static void assertZipEquals(Address expected, Address actual)
    {
        assertEquals(expected.getZip5().toUpperCase(), actual.getZip5().toUpperCase());
        if (!expected.getZip4().isEmpty() && !actual.getZip4().isEmpty()){
            assertEquals(expected.getZip4().toUpperCase(), actual.getZip4().toUpperCase());
        }
    }


}
