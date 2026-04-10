package gov.nysenate.sage.model.stats;

import gov.nysenate.sage.provider.geocode.Geocoder;

import java.util.HashMap;
import java.util.Map;

public class GeocodeStats {
    private final Map<Geocoder, Integer> geocoderUsage = new HashMap<>();
    private int totalRequests = 0;
    private int totalGeocodes = 0;
    private int totalCacheHits = 0;

    public void addGeocoderUsage(Geocoder geocoder, boolean succeeded, int usage) {
        geocoderUsage.merge(geocoder, usage, Integer::sum);
        if (geocoder == Geocoder.GEOCACHE && succeeded) {
            totalCacheHits += usage;
        }
        totalGeocodes += usage;
        totalRequests += usage;
    }

    public Map<Geocoder, Integer> getGeocoderUsage() {
        return geocoderUsage;
    }

    public int getTotalRequests() {
        return totalRequests;
    }

    public int getTotalGeocodes() {
        return totalGeocodes;
    }

    public int getTotalCacheHits() {
        return totalCacheHits;
    }
}
