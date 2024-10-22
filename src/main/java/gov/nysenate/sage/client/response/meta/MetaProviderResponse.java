package gov.nysenate.sage.client.response.meta;

import gov.nysenate.sage.provider.geocode.Geocoder;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

public class MetaProviderResponse {
    private final List<String> geocoders;

    public MetaProviderResponse(@Nonnull Set<Geocoder> geocoders) {
        this.geocoders = geocoders.stream().map(Enum::toString).toList();
    }

    public List<String> getGeocoders() {
        return geocoders;
    }
}
