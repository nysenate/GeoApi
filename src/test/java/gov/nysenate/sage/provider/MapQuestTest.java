package gov.nysenate.sage.provider;

import static gov.nysenate.sage.AddressTestBase.*;
import static gov.nysenate.sage.GeocodeTestBase.*;
import gov.nysenate.sage.TestBase;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

public class MapQuestTest extends TestBase
{
    private static Logger logger = Logger.getLogger(MapQuestTest.class);
    private MapQuest mapQuest;

    @Before
    public void setUp()
    {
        mapQuest = new MapQuest();
    }

    @Test
    public void singleGeocode_ReturnsGeocodeResult() throws Exception
    {
        assertSingleAddressGeocode(mapQuest);
    }

    @Test
    public void multipleGeocode_ReturnsGeocodeResults() throws Exception
    {
        assertMultipleAddressGeocode(mapQuest);
    }

    @Test
    public void singleReverseGeocode_ReturnsGeocodeResult() throws Exception
    {
        assertSingleReverseGeocode(mapQuest);
    }

    @Test
    public void invalidInputGeocode_ReturnsErrorStatus()
    {
        assertNoResultReturnsNoGeocodeResultStatus(mapQuest);
    }

    @Test
    public void singleAddressValidate_ReturnsAddressResult() throws Exception
    {
        assertSingleAddressValidation(mapQuest);
    }

    @Test
    public void multipleAddressValidate_ReturnsAddressResult() throws Exception
    {
        assertMultiAddressValidation(mapQuest);
    }

    @Test
    public void badAddressValidate_ReturnsAddressResultError() throws Exception
    {
        assertMultiAddressValidation(mapQuest);
    }

    /** Not sure if necessary, simply proxies to validate */
    @Test
    public void testLookupCityState_ReturnsAddressResult() throws Exception
    {
        assertCityStateLookup(mapQuest);
    }

    /** Not sure if necessary, simply proxies to validate */
    @Test
    public void testLookupZipCode() throws Exception {

    }


}