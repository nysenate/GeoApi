package gov.nysenate.sage.dao.provider.nysgeo;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.geo.Point;

public interface NYSGeoDao {

    /**
     * This method performs geocoding.
     * Retrieves a GeocodedAddress given an Address using the NYS geocoder.
     *
     * @param address   Address to geocode
     * @return          GeocodedAddress containing best matched Geocode.
     *                  null if there was a fatal error
     */
    public GeocodedAddress getGeocodedAddress(Address address);

    /**
     * This method performs geocoding.
     * Retrieves a GeocodedAddress given an Address using the NYS geocoder.
     *
     * @param point   Point to geocode
     * @return          GeocodedAddress containing best matched Geocode.
     *                  null if there was a fatal error
     */
    public GeocodedAddress getGeocodedAddress(Point point);


}
