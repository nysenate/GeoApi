package gov.nysenate.sage.model.stats;

import java.util.HashMap;
import java.util.Map;

public class GeocodeStats
{
    private Map<String, Integer> geocoderUsage;

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
}
