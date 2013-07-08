package gov.nysenate.sage.model.address;

import gov.nysenate.sage.TestBase;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;
import gov.nysenate.sage.model.geo.Point;
import org.junit.Test;

import static org.junit.Assert.*;

public class GeocodedAddressTest extends TestBase
{
    @Test
    public void isGeocodedTest()
    {
        GeocodedAddress geocodedAddress = new GeocodedAddress();
        geocodedAddress.setAddress(new Address("Some addresss", "Some town", "NY", "12313"));

        geocodedAddress.setGeocode(new Geocode(new Point(1,1)));
        assertTrue(geocodedAddress.isValidGeocode());

        geocodedAddress.setGeocode(new Geocode(new Point(1,1), GeocodeQuality.HOUSE));
        assertTrue(geocodedAddress.isValidGeocode());

        geocodedAddress.setGeocode(new Geocode(new Point(0,0), GeocodeQuality.NOMATCH));
        assertFalse(geocodedAddress.isValidGeocode());

        geocodedAddress.setGeocode(new Geocode());
        assertFalse(geocodedAddress.isValidGeocode());

        geocodedAddress.setGeocode(null);
        assertFalse(geocodedAddress.isValidGeocode());
    }

    @Test
    public void isReverseGeocodedTest()
    {
        GeocodedAddress geocodedAddress = new GeocodedAddress();
        geocodedAddress.setGeocode(new Geocode(new Point(1,1)));

        geocodedAddress.setAddress(new Address("Some addresss", "Some town", "NY", "12313"));
        assertTrue(geocodedAddress.isReverseGeocoded());

        geocodedAddress.setAddress(new Address("Some addresss"));
        assertTrue(geocodedAddress.isReverseGeocoded());

        geocodedAddress.setAddress(new Address("", "", "", ""));
        assertFalse(geocodedAddress.isReverseGeocoded());

        geocodedAddress.setAddress(new Address());
        assertFalse(geocodedAddress.isReverseGeocoded());

        geocodedAddress.setAddress(new Address(null));
        assertFalse(geocodedAddress.isReverseGeocoded());
    }
}
