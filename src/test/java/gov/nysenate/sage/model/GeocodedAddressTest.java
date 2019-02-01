package gov.nysenate.sage.model;

import gov.nysenate.sage.annotation.UnitTest;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;
import gov.nysenate.sage.model.geo.Point;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(UnitTest.class)
public class GeocodedAddressTest {

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

}
