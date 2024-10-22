package gov.nysenate.sage.provider.geocache;

import gov.nysenate.sage.model.result.GeocodeResult;

import java.util.List;

/**
 * Interface for geocode caching.
 */
public interface GeocodeCacheService {
    void saveToCacheAndFlush(GeocodeResult geocodeResult);
    void saveToCacheAndFlush(List<GeocodeResult> geocodeResults);
}
