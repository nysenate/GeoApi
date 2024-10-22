package gov.nysenate.sage.service.district;

import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.provider.district.DistrictService;

import java.util.List;

public interface SageParallelDistrictService {
    /**
     * Assign a list of geocoded addresses with a separate thread
     */
    List<DistrictResult> assignDistricts(DistrictService districtService,
                                         List<GeocodedAddress> geocodedAddresses, List<DistrictType> types);

    /**
     * Shutdown the threads being used for the parallel district assignment
     */
    void shutdownThread();
}
