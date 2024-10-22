package gov.nysenate.sage.service.geo;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.provider.geocode.GeocodeService;

import java.util.List;

public interface SageParallelGeocodeService {
    /**
     * Geocodes a list of addresses in parallel.
     */
    List<GeocodeResult> geocode(GeocodeService geocodeService, List<Address> addresses);
}
