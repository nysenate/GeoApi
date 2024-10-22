package gov.nysenate.sage.dao.provider.geocache;

import gov.nysenate.sage.model.address.GeocodedAddress;

import java.util.List;

public interface GeoCacheDao {
    /**
     * Pushes a geocoded address to the buffer for saving to cache.
     * @param geocodedAddress GeocodedAddress to cache.
     */
    void cacheGeocodedAddress(GeocodedAddress geocodedAddress);

    /**
     * Pushes a list of geocoded addresses to the buffer for saving to cache.
     * @param geocodedAddresses GeocodedAddress List to cache.
     */
    default void cacheGeocodedAddresses(List<GeocodedAddress> geocodedAddresses) {
        for (GeocodedAddress geocodedAddress : geocodedAddresses) {
            cacheGeocodedAddress(geocodedAddress);
        }
    }
}
