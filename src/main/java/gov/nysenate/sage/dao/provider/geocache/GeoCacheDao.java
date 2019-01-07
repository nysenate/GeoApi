package gov.nysenate.sage.dao.provider.geocache;

import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.address.GeocodedStreetAddress;
import gov.nysenate.sage.model.address.StreetAddress;

import java.util.List;

public interface GeoCacheDao {

    /**
     * Performs a lookup on the cache table and returns a GeocodedStreetAddress upon match.
     * @param sa  StreetAddress to lookup
     * @return    GeocodedStreetAddress
     */
    public GeocodedStreetAddress getCacheHit(StreetAddress sa);

    /**
     * Pushes a geocoded address to the buffer for saving to cache.
     * @param geocodedAddress GeocodedAddress to cache.
     */
    public void cacheGeocodedAddress(GeocodedAddress geocodedAddress);

    /**
     * Pushes a list of geocoded addresses to the buffer for saving to cache.
     * @param geocodedAddresses GeocodedAddress List to cache.
     */
    public void cacheGeocodedAddresses(List<GeocodedAddress> geocodedAddresses);




}
