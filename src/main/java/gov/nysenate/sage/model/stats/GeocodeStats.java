package gov.nysenate.sage.model.stats;

import java.util.HashMap;
import java.util.Map;

public class GeocodeStats
{
    private Map<String, Integer> geocoderUsage;
    private int totalRequests;
    private int totalGeocodes;
    private int totalCacheHits;

    public GeocodeStats() {
        this.geocoderUsage = new HashMap<>();
    }

    public void addGeocoderUsage(String geocoder, int usage) {
        this.geocoderUsage.put(geocoder, usage);
    }

    public Map<String, Integer> getGeocoderUsage() {
        return this.geocoderUsage;
    }

    public void setGeocoderUsage(Map<String, Integer> geocoderUsage) {
        this.geocoderUsage = geocoderUsage;
    }

    public int getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(int totalRequests) {
        this.totalRequests = totalRequests;
    }

    public int getTotalGeocodes() {
        return totalGeocodes;
    }

    public void setTotalGeocodes(int totalGeocodes) {
        this.totalGeocodes = totalGeocodes;
    }

    public int getTotalCacheHits() {
        return totalCacheHits;
    }

    public void setTotalCacheHits(int totalCacheHits) {
        this.totalCacheHits = totalCacheHits;
    }
}
