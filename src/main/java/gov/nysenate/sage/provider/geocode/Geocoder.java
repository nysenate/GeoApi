package gov.nysenate.sage.provider.geocode;

import java.util.LinkedHashSet;
import java.util.List;

public enum Geocoder implements DataSource {
    GEOCACHE, NYSGEO, GOOGLE;

    public static List<Geocoder> getGeocoders(Geocoder baseProvider, boolean useCache, boolean useFallback) {
        var geocoders = new LinkedHashSet<Geocoder>();
        if (baseProvider == null) {
            throw new IllegalArgumentException("baseProvider cannot be null");
        }
        if (baseProvider == GEOCACHE && !useCache) {
            throw new IllegalArgumentException("If a provider is not specified, the cache must be allowed.");
        }
        geocoders.add(baseProvider);
        if (useFallback) {
            geocoders.addAll(List.of(values()));
        }
        if (!useCache) {
            geocoders.remove(GEOCACHE);
        }
        return List.copyOf(geocoders);
    }
}
