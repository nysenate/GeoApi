package gov.nysenate.sage.service.geo;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.provider.geocode.GeocodeService;

import java.util.ArrayList;
import java.util.List;

public interface SageParallelGeocodeService {

    /**
     * Geocodes a list of addresses in parallel with
     * @param geocodeService
     * @param addresses
     * @return
     */
    public ArrayList<GeocodeResult> geocode(GeocodeService geocodeService, List<Address> addresses);

    /**
     * Shutdown the threads being used for the parallel geocoding
     */
    public void shutdownThread();
}
