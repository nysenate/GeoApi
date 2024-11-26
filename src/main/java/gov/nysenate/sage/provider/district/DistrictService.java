package gov.nysenate.sage.provider.district;

import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.service.district.ParallelDistrictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * DistrictService is used to assign district information to addresses and may or may not require
 * geo-coordinate information.
 * Also provides the method to be called through ParallelDistrictService.
 */
@Service
public abstract class DistrictService {
    @Autowired
    private ParallelDistrictService parallelDistrictService;

    public abstract DistrictSource districtSource();

    public abstract DistrictResult assignDistricts(GeocodedAddress geocodedAddress, List<DistrictType> reqTypes);

    public List<DistrictResult> assignDistricts(List<GeocodedAddress> geocodedAddresses, List<DistrictType> reqTypes) {
        return parallelDistrictService.assignDistricts(this, geocodedAddresses, reqTypes);
    }

    /** Assignment method to be called when using ParallelDistrictService.
     *  May simply just be a delegate to assignDistricts depending on the implementation. */
    public DistrictResult assignDistrictsForBatch(GeocodedAddress geocodedAddress, List<DistrictType> reqTypes) {
        return assignDistricts(geocodedAddress, reqTypes);
    }
}
