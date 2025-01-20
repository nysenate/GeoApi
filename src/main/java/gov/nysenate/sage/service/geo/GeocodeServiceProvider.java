package gov.nysenate.sage.service.geo;

import gov.nysenate.sage.provider.geocode.Geocoder;

import java.util.Set;

public interface GeocodeServiceProvider {
    /**
     * Return a map containing a Geocoder and its service.
     */
    Set<Geocoder> geocoders();
}
