package gov.nysenate.sage.provider.geocache;

import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.provider.geocode.GeocodeService;

import java.util.List;

/**
 * Interface for geocode caching.
 */
public interface GeocodeCacheService extends GeocodeService
{
    public void saveToCache(GeocodeResult geocodeResult);
    public void saveToCacheAndFlush(GeocodeResult geocodeResult);
    public void saveToCache(List<GeocodeResult> geocodeResults);
    public void saveToCacheAndFlush(List<GeocodeResult> geocodeResults);
}
