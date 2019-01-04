package gov.nysenate.sage.dao.provider.google;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.geo.Point;

public interface GoogleDao {

    /**
     * Get the url string for contacting googles api
     * @return
     */
    public String getBaseUrl();

    /**
     * This method performs geocoding.
     * Retrieves a GeocodedAddress given an Address using Google.
     *
     * @param address   Address to geocode
     * @return          GeocodedAddress containing best matched Geocode.
     *                  null if there was a fatal error
     */
    public GeocodedAddress getGeocodedAddress(Address address);

    /**
     * This method performs reverse geocoding.
     * Retrieves a GeocodedAddress given a Point using Google.
     *
     * @param point Point to reverse geocode.
     * @return      GeocodedAddress containing best matched Address.
     */
    public GeocodedAddress getGeocodedAddress(Point point);
}
