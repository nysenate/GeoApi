package gov.nysenate.sage.service.geo;

import gov.nysenate.sage.model.result.GeocodeResult;

import java.util.List;

public interface GeocodeCacheService extends GeocodeService
{
    public void saveToCache(GeocodeResult geocodeResult);
    public void saveToCache(List<GeocodeResult> geocodeResults);
}
