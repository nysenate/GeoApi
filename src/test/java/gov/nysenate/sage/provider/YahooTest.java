package gov.nysenate.sage.provider;

import gov.nysenate.sage.TestBase;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.util.FormatUtil;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import static gov.nysenate.sage.GeocodeTestBase.*;
import static org.junit.Assert.assertEquals;

public class YahooTest extends TestBase
{
//    private Logger logger = Logger.getLogger(YahooTest.class);
//    private Yahoo yahoo;
//
//    @Before
//    public void setUp()
//    {
//        yahoo = new Yahoo();
//    }
//
//    @Test
//    public void singleGeocode_ReturnsGeocodeResult()
//    {
//        assertSingleAddressGeocode(yahoo);
//    }
//
//    @Test
//    public void multipleGeocode_ReturnsGeocodeResults()
//    {
//        assertMultipleAddressGeocode(yahoo);
//    }
//
//    @Test
//    public void singleReverseGeocode_ReturnsGeocodeResult()
//    {
//        assertSingleReverseGeocode(yahoo);
//    }
//
//    @Test
//    public void geocodeErrorCases_SetsResultStatus()
//    {
//        assertNoResultReturnsNoGeocodeResultStatus(yahoo);
//        assertNullAddressReturnsMissingAddressStatus(yahoo);
//        assertEmptyAddressReturnsInsufficientAddressStatus(yahoo);
//    }
//
//    @Test
//    public void invalidPointReverseGeocode_ReturnsNoReverseGeocodeStatus()
//    {
//        GeocodeResult geocodeResult = yahoo.reverseGeocode(new Point(-230,430));
//        FormatUtil.printObject(geocodeResult);
//        assertEquals(ResultStatus.NO_REVERSE_GEOCODE_RESULT, geocodeResult.getStatusCode());
//    }
//
//    @Test
//    public void test()
//    {
//        FormatUtil.printObject(yahoo.geocode(new Address("72-61 113th Street", "Forest Hills", "NY", "11375")));
//    }
}
