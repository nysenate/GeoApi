package gov.nysenate.sage.service.geo;

import gov.nysenate.sage.provider.geocode.Geocoder;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Point of access for all geocoding requests. This class maintains a collection of available
 * geocoding providers and contains logic for distributing requests and collecting responses
 * from the providers.
 */
@Service
public class SageGeocodeServiceProvider implements GeocodeServiceProvider {
    // TODO: remove
    public Set<Geocoder> geocoders() {
        return Set.of(Geocoder.values());
    }
}
