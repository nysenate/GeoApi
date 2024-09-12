package gov.nysenate.sage.dao.provider.mapquest;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.geo.Point;

import java.util.List;

public interface MapQuestDao {
    /**
     * This method performs batch geocoding.
     * Retrieves a GeocodedAddress given an Address using Yahoo.
     *
     * @param addresses Addresses to geocode
     * @return          ArrayList of GeocodedAddress containing best matched Geocodes.
     */
    List<GeocodedAddress> getGeocodedAddresses(List<Address> addresses);

    /**
     * Returns a geocoded address from a lat lon coordinates in a point object
     */
    GeocodedAddress getGeocodedAddress(Point point);
}
