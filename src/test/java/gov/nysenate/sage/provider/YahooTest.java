package gov.nysenate.sage.provider;

import gov.nysenate.sage.TestBase;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import static gov.nysenate.sage.GeocodeTestBase.assertMultipleAddressGeocode;
import static gov.nysenate.sage.GeocodeTestBase.assertSingleAddressGeocode;
import static gov.nysenate.sage.GeocodeTestBase.assertSingleReverseGeocode;

public class YahooTest extends TestBase
{
    private Logger logger = Logger.getLogger(YahooTest.class);
    private Yahoo yahoo;

    @Before
    public void setUp()
    {
        yahoo = new Yahoo();
    }

    @Test
    public void YahooSingleGeocode_ReturnsGeocodeResult()
    {
        assertSingleAddressGeocode(yahoo);
    }

    @Test
    public void YahooMultipleGeocode_ReturnsGeocodeResults()
    {
        assertMultipleAddressGeocode(yahoo);
    }

    @Test
    public void YahooSingleReverseGeocode_ReturnsGeocodeResult()
    {
        assertSingleReverseGeocode(yahoo);
    }
}
