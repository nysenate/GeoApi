package gov.nysenate.sage.dao.provider.mapquest;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.geo.Point;

import java.util.ArrayList;
import java.util.List;

public interface MapQuestDao {

    /**
     * Getter for the geoUrl variable
     * @return
     */
    public String getGeoUrl();

    /**
     * Setter for the geoUrl variable
     * @param geoUrl
     */
    public void setGeoUrl(String geoUrl);

    /**
     * Getter for the revGeoUrl variable
     * @return
     */
    public String getRevGeoUrl();

    /**
     * Setter for the revGeoUrl variable
     * @param revGeoUrl
     */
    public void setRevGeoUrl(String revGeoUrl);

    /**
     * Getter for the variable Key
     * @return
     */
    public String getKey();

    /**
     * Setter for the variable Key
     * @param key
     */
    public void setKey(String key);

    /**
     * This method performs batch geocoding.
     * Retrieves a GeocodedAddress given an Address using Yahoo.
     *
     * @param addresses Addresses to geocode
     * @return          ArrayList of GeocodedAddress containing best matched Geocodes.
     */
    public List<GeocodedAddress> getGeocodedAddresses(ArrayList<Address> addresses);

    /**
     * Returns a geocoded address from a lat lon coordinates in a point object
     * @param point
     * @return
     */
    public GeocodedAddress getGeocodedAddress(Point point);
}
