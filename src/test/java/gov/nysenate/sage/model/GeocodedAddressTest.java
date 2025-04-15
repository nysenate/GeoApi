package gov.nysenate.sage.model;

import gov.nysenate.sage.annotation.UnitTest;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.provider.geocode.Geocoder;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;

@Category(UnitTest.class)
public class GeocodedAddressTest {
    @Test
    public void isGeocodedTest() {
        testGeocode(new Geocode(new Point("1", "1"), GeocodeQuality.POINT, Geocoder.NYSGEO, false), true);
        testGeocode(new Geocode(new Point("1", "1"), GeocodeQuality.HOUSE, Geocoder.NYSGEO, false), true);
        testGeocode(new Geocode(new Point("0", "0"), GeocodeQuality.NOMATCH, Geocoder.NYSGEO, false), false);
        testGeocode(null, false);
    }

    private static void testGeocode(Geocode geocode, boolean isValid) {
        var geoAddr = new GeocodedAddress(geocode);
        assertEquals(isValid, geoAddr.isValidGeocode());
    }
}
