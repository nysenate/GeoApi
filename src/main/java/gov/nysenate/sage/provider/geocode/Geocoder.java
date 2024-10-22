package gov.nysenate.sage.provider.geocode;

import java.util.LinkedHashSet;
import java.util.List;

public enum Geocoder implements DataSource {
    GEOCACHE, GOOGLE, NYSGEO;

    public static List<Geocoder> getGeocoders(String providerStr, boolean useCache, boolean useFallback) {
        var geocoders = new LinkedHashSet<Geocoder>();
        Geocoder provider;
        try {
            provider = valueOf(providerStr.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException ignored) {
            provider = GEOCACHE;
        }
        if (provider == GEOCACHE && !useCache) {
            throw new IllegalArgumentException("If a provider is not specified, the cache must be allowed.");
        }
        geocoders.add(provider);
        if (useFallback) {
            geocoders.addAll(List.of(values()));
        }
        if (!useCache) {
            geocoders.remove(GEOCACHE);
        }
        return List.copyOf(geocoders);
    }
}
