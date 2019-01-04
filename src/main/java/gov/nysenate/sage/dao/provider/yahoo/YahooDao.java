package gov.nysenate.sage.dao.provider.yahoo;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.geo.Point;

import java.util.List;

public interface YahooDao {

    /**
     * Get a string which contains the url for connecting to yahoo
     * @return
     */
    public String getBaseUrl();

    /**
     * This method performs geocoding.
     * Retrieves a GeocodedAddress given an Address using Yahoo.
     *
     * @param address   Address to geocode
     * @return          GeocodedAddress containing best matched Geocode.
     *                  null if there was a fatal error
     */
    public GeocodedAddress getGeocodedAddress(Address address);

    /**
     * Batch geocode a list of addresses.
     * @param addresses Addresses to geocode
     * @return          List<GeocodedAddress>
     */
    public List<GeocodedAddress> getGeocodedAddresses(List<Address> addresses);

    /**
     * This method performs reverse geocoding.
     * Retrieves a GeocodedAddress given a Point using Yahoo.
     *
     * @param point Point to reverse geocode.
     * @return      GeocodedAddress containing best matched Address.
     */
    public GeocodedAddress getGeocodedAddress(Point point);


}
