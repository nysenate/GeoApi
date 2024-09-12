package gov.nysenate.sage.provider.geocache;

import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.provider.geocode.GeocodeService;

import java.util.List;

/**
 * Interface for geocode caching.
 */
public interface GeocodeCacheService extends GeocodeService {
    void saveToCacheAndFlush(GeocodeResult geocodeResult);
    void saveToCacheAndFlush(List<GeocodeResult> geocodeResults);
}
