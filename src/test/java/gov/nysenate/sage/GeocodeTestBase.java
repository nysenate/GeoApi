package gov.nysenate.sage;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.service.geo.GeocodeService;
import gov.nysenate.sage.service.geo.RevGeocodeService;
import gov.nysenate.sage.util.FormatUtil;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static gov.nysenate.sage.AddressTestBase.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public abstract class GeocodeTestBase
{
    private static Logger logger = Logger.getLogger(GeocodeTestBase.class);
    /** Indicates how far apart two lat or lon values can be and yet considered similar. */
    private static double GEOCODE_EPSILON = 0.2;

    public static ArrayList<Address> addresses = new ArrayList<>(Arrays.asList(
            new Address("214 8th Street", "", "Troy", "NY", "12180", ""),
            new Address("101 East State Street", "", "Olean", "NY", "14760", ""),
            new Address("2012 E Rivr Road", "", "Olean", "NY", "14760", ""),
            new Address("44 Fairlawn Ave", "Apt 2B", "Albany", "NY", "12203", ""),
            new Address("18 Greenhaven Dr", "" ,"Port Jefferson Station", "NY", "11776", ""),
            new Address("479 Deer Park AVE","", "Babylon", "NY", "11702", "")));

    public static ArrayList<Geocode> expectedGeocode = new ArrayList<>(Arrays.asList(
            new Geocode(new Point(42.735240846109576, -73.68281736214925), GeocodeQuality.POINT, "Test"),
            new Geocode(new Point(42.07758488174446, -78.42985558117132), GeocodeQuality.POINT, "Test"),
            new Geocode(new Point(42.06857062361315, -78.41382617654084), GeocodeQuality.POINT, "Test"),
            new Geocode(new Point(42.67166963437981, -73.79857701653509), GeocodeQuality.POINT, "Test"),
            new Geocode(new Point(40.91447799244534, -73.05684231135687), GeocodeQuality.POINT, "Test"),
            new Geocode(new Point(40.70562763769997, -73.32196531833924), GeocodeQuality.POINT, "Test")));

    public static void assertSingleAddressGeocode(GeocodeService geocodeService)
    {
        Address address = new Address("44 Fairlawn Ave", "", "Albany", "NY", "12203", "");
        Point latlon = new Point(42.671669, -73.798577);

        assertSingleAddressGeocode(geocodeService, address, latlon);
    }

    public static void assertSingleAddressGeocode(GeocodeService geocodeService, Address address, Point latlon)
    {
        Geocode expectedCode = new Geocode(latlon, GeocodeQuality.POINT, "TEST");
        GeocodeResult geocodeResult = geocodeService.geocode(address);

        assertNotNull(geocodeResult);
        Geocode actualCode = geocodeResult.getGeocodedAddress().getGeocode();

        /** Make sure the quality is not NOMATCH */
        assertNotSame(GeocodeQuality.NOMATCH, actualCode.getQuality());

        /** Check that the method is not null */
        assertNotNull(actualCode.getMethod());

        /** Check that the latlon pairs are similar to the expected result */
        assertGeocodesAreSimilar(expectedCode, actualCode);

        logger.debug(FormatUtil.toJsonString(geocodeResult));
    }

    public static void assertMultipleAddressGeocode(GeocodeService geocodeService)
    {
        ArrayList<Address> addresses = new ArrayList<>(Arrays.asList(
                new Address("214 8th Street", "", "Troy", "NY", "12180", ""),
                new Address("101 East State Street", "", "Olean", "NY", "14760", ""),
                new Address("2012 E Rivr Road", "", "Olean", "NY", "14760", ""),
                new Address("44 Fairlawn Ave", "Apt 2B", "Albany", "NY", "12203", ""),
                new Address("18 Greenhaven Dr", "" ,"Port Jefferson Station", "NY", "11776", "")));

        ArrayList<Geocode> expectedGeocode = new ArrayList<>(Arrays.asList(
                new Geocode(new Point(42.735240846109576, -73.68281736214925), null, null),
                new Geocode(new Point( 42.07758488174446, -78.42985558117132), null, null),
                new Geocode(new Point(42.06857062361315, -78.41382617654084), null, null),
                new Geocode(new Point(42.67166963437981, -73.79857701653509), null, null),
                new Geocode(new Point(40.91447799244534, -73.05684231135687), null, null)));

        ArrayList<GeocodeResult> geocodeResults = geocodeService.geocode(addresses);
        logger.debug(FormatUtil.toJsonString(geocodeResults));

        assertNotNull(geocodeResults);
        assertEquals(5, geocodeResults.size());

        for (int i = 0; i < addresses.size(); i++){
            Geocode geocode = geocodeResults.get(i).getGeocodedAddress().getGeocode();
            assertFalse(GeocodeQuality.NOMATCH == geocode.getQuality());
            assertGeocodesAreSimilar(expectedGeocode.get(i), geocode);
        }
    }

    public static void assertSingleReverseGeocode(RevGeocodeService revGeocodeService)
    {
        /** This is not an accuracy test, but rather a check to see if the address returned is reasonable */
        GeocodeResult geocodeResult = revGeocodeService.reverseGeocode(new Point(42.6716696, -73.7985770));
        assertNotNull(geocodeResult);

        Address expected = new Address("44 Fairlawn Ave", "", "Albany", "NY", "12203", "");
        Address address = geocodeResult.getGeocodedAddress().getAddress();
        assertNotNull(address);
        assertCityStateEquals(expected, address);
        assertZipEquals(expected, address);
    }

    /** Test Error Cases */
    public static void assertNoResultReturnsNoGeocodeResultStatus(GeocodeService geocodeService)
    {
        GeocodeResult geocodeResult = geocodeService.geocode(new Address("BLAH"));
        assertEquals(ResultStatus.NO_GEOCODE_RESULT, geocodeResult.getStatusCode());
    }

    public static void assertNullAddressReturnsMissingAddressStatus(GeocodeService geocodeService)
    {
        Address nullAddress = null;
        GeocodeResult geocodeResult = geocodeService.geocode(nullAddress);
        assertEquals(ResultStatus.MISSING_ADDRESS, geocodeResult.getStatusCode());
    }

    public static void assertEmptyAddressReturnsInsufficientAddressStatus(GeocodeService geocodeService)
    {
        Address emptyAddress = new Address();
        GeocodeResult geocodeResult = geocodeService.geocode(emptyAddress);
        assertEquals(ResultStatus.INSUFFICIENT_ADDRESS, geocodeResult.getStatusCode());
    }

    /** Test if geocodes are returning reasonable results but they do not necessarily have
     *  to be exact.
     * @param expected
     * @param actual
     */
    public static void assertGeocodesAreSimilar(Geocode expected, Geocode actual)
    {
        assertTrue((expected.getLat() - actual.getLat()) < GEOCODE_EPSILON);
        assertTrue((expected.getLon() - actual.getLon()) < GEOCODE_EPSILON);
    }
}
