package gov.nysenate.sage.provider;

import gov.nysenate.sage.TestBase;
import gov.nysenate.sage.adapter.OSM;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.util.FormatUtil;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import static gov.nysenate.sage.GeocodeTestBase.assertMultipleAddressGeocode;
import static gov.nysenate.sage.GeocodeTestBase.assertSingleAddressGeocode;

public class OSM_Test extends TestBase
{
    private Logger logger = Logger.getLogger(YahooTest.class);
    private OSM osm;

    @Before
    public void setUp()
    {
        osm = new OSM();
    }

    @Test
    public void OSMSingleGeocode_ReturnsGeocodeResult()
    {
        assertSingleAddressGeocode(osm);
    }

    @Test
    public void OSMMultipleGeocode_ReturnsGeocodeResult()
    {
        assertMultipleAddressGeocode(osm);
    }
}
