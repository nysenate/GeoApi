package gov.nysenate.sage.service.district;

import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.api.BatchDistrictRequest;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.provider.district.DistrictSource;

import java.util.List;

public interface SageDistrictServiceProvider {
    /**
     * If a district provider is specified use that for district assignment.
     * Otherwise, the default strategy for district assignment is to run both street file and district shape file
     * look-ups in parallel. Once results from both lookup methods are retrieved they are compared and consolidated.
     */
    DistrictResult assignDistricts(final GeocodedAddress geocodedAddress, final List<DistrictSource> distProviders,
                                          final List<DistrictType> districtTypes);

    /**
     * Assign standard districts with options set in BatchDistrictRequest.
     */
    List<DistrictResult> assignDistricts(final BatchDistrictRequest bdr);
}
