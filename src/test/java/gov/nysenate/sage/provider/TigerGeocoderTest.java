package gov.nysenate.sage.provider;

import gov.nysenate.sage.TestBase;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.util.FormatUtil;
import org.junit.Before;
import org.junit.Test;

import static gov.nysenate.sage.GeocodeTestBase.*;
import static org.junit.Assert.*;

public class TigerGeocoderTest extends TestBase
{
    private TigerGeocoder tigerGeocoder;

    @Before
    public void setUp()
    {
        tigerGeocoder = new TigerGeocoder();
    }

    @Test
    public void singleAddressGeocode_ReturnsGeocodeResult() throws Exception
    {
        assertSingleAddressGeocode(tigerGeocoder);
    }

    @Test
    public void multipleAddressGeocode_ReturnsGeocodeResults() throws Exception
    {
        assertMultipleAddressGeocode(tigerGeocoder);
    }

    @Test
    public void singleReverseGeocode_ReturnsGeocodeResult() throws Exception
    {
        assertSingleReverseGeocode(tigerGeocoder);
    }

    @Test
    public void noMatchGeocode_ReturnsNoGeocodeResponseStatus()
    {
        Address incorrectAddress = new Address("9264 224 st", "Queens Village", "NY", "11432");
        GeocodeResult geocodeResult = tigerGeocoder.geocode(incorrectAddress);
        FormatUtil.printObject(geocodeResult);
        assertEquals(ResultStatus.NO_GEOCODE_RESULT, geocodeResult.getStatusCode());
    }

    @Test
    public void invalidInputGeocode_ReturnsErrorStatus()
    {
        assertNoResultReturnsNoGeocodeResultStatus(tigerGeocoder);
        assertNullAddressReturnsMissingAddressStatus(tigerGeocoder);
        assertEmptyAddressReturnsInsufficientAddressStatus(tigerGeocoder);
    }

    @Test
    public void invalidPointReverseGeocode_ReturnsNoReverseGeocodeStatus()
    {
        GeocodeResult geocodeResult = tigerGeocoder.reverseGeocode(new Point(1,1));
        assertEquals(ResultStatus.NO_REVERSE_GEOCODE_RESULT, geocodeResult.getStatusCode());
    }
}
