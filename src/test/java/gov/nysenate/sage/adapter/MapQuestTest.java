package gov.nysenate.sage.adapter;

import static gov.nysenate.sage.AddressTestBase.*;
import gov.nysenate.sage.TestBase;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.result.AddressResult;
import gov.nysenate.sage.provider.USPS;
import gov.nysenate.sage.util.FormatUtil;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class MapQuestTest extends TestBase
{
    private Logger logger = Logger.getLogger(MapQuestTest.class);
    private MapQuest mapQuest;

    @Before
    public void setUp()
    {
        mapQuest = new MapQuest();
    }


    @Test
    public void MapQuestSingleGeocode_ReturnsGeocodeResult() throws Exception
    {

    }

    @Test
    public void MapQuestMultipleGeocode_ReturnsGeocodeResults() throws Exception
    {

    }

    @Test
    public void testReverseGeocode() throws Exception
    {

    }

    @Test
    public void MapQuestSingleAddressValidate_ReturnsAddressResult() throws Exception
    {
        Address address = new Address("44 Fairlawn", "", "Albany", "ny", "12203", "");
        AddressResult addressResult = mapQuest.validate(address);

        logger.debug(FormatUtil.toJsonString(addressResult));

        assertNotNull(addressResult);
        assertNotNull(addressResult.getAddress());
        assertEquals("44 FAIRLAWN AVE", addressResult.getAddress().getAddr1().toUpperCase());
        assertEquals("ALBANY", addressResult.getAddress().getCity().toUpperCase());
        assertEquals("NY", addressResult.getAddress().getState().toUpperCase());
        assertEquals("12203", addressResult.getAddress().getZip5().toUpperCase());
        assertEquals("1914", addressResult.getAddress().getZip4().toUpperCase());
    }

    @Test
    public void MapQuestMultipleAddressValidate_ReturnsAddressResult() throws Exception
    {
        ArrayList<Address> addresses = new ArrayList<>(Arrays.asList(
                new Address("71 14th Street", "", "Troy", "NY", "", ""),
                new Address("214 8th Street", "", "Troy", "NY", "12180", ""),
                new Address("101 East State Street", "", "Olean", "NY", "14760", ""),
                new Address("Oak Hill Park", "", "Olean", "NY", "14760", ""), /** This one will be invalid */
                new Address("2012 E Rivr Road", "", "Olean", "NY", "14760", ""),
                new Address("44 Fairlawn Ave", "Apt 2B", "Albany", "NY", "12203", "")
        ));

        ArrayList<AddressResult> addressResults = mapQuest.validate(addresses);

        logger.debug(FormatUtil.toJsonString(addressResults));

        assertEquals(6, addressResults.size());
        assertEquals(MapQuest.class.getSimpleName(), addressResults.get(0).getSource());

        Address expected = new Address("71 14TH ST", "", "TROY", "NY", "12180", "4209");
        assertTrue(addressResults.get(0).isValidated());
        assertAddressEquals(expected, addressResults.get(0).getAddress());

        expected = new Address("214 8TH ST", "", "TROY", "NY", "12180", "2931");
        assertTrue(addressResults.get(1).isValidated());
        assertAddressEquals(expected, addressResults.get(1).getAddress());

        expected = new Address("101 E STATE ST", "", "OLEAN", "NY", "14760", "2776");
        assertTrue(addressResults.get(2).isValidated());
        assertAddressEquals(expected, addressResults.get(2).getAddress());

        expected = new Address("2012 E RIVER RD", "", "OLEAN", "NY", "14760", "9309");
        assertTrue(addressResults.get(4).isValidated());
        assertAddressEquals(expected, addressResults.get(4).getAddress());

        expected = new Address("44 FAIRLAWN AVE APT 2B", "", "ALBANY", "NY", "12203", "1914");
        assertTrue(addressResults.get(5).isValidated());
        assertAddressEquals(expected, addressResults.get(5).getAddress());
    }

    /** Not sure if necessary, simply proxies to validate */
    @Test
    public void testLookupCityState() throws Exception {

    }

    /** Not sure if necessary, simply proxies to validate */
    @Test
    public void testLookupZipCode() throws Exception {

    }


}
