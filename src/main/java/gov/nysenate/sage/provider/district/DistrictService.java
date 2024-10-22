package gov.nysenate.sage.provider.district;

import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.service.district.ParallelDistrictService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static gov.nysenate.sage.model.district.DistrictType.*;

/**
 * DistrictService is used to assign district information to addresses and may or may not require
 * geo-coordinate information.
 * Also provides the method to be called through ParallelDistrictService.
 */
@Service
public abstract class DistrictService {
    private static final Logger logger = LoggerFactory.getLogger(DistrictService.class);
    private static final List<DistrictType> requiredTypes = List.of(ASSEMBLY, CONGRESSIONAL, SENATE, COUNTY);
    @Autowired
    private ParallelDistrictService parallelDistrictService;

    public abstract DistrictSource districtSource();

    public abstract DistrictResult assignDistricts(GeocodedAddress geocodedAddress, List<DistrictType> reqTypes);

    public DistrictResult assignDistricts(GeocodedAddress geocodedAddress) {
        return assignDistricts(geocodedAddress, requiredTypes);
    }

    public List<DistrictResult> assignDistricts(List<GeocodedAddress> geocodedAddresses, List<DistrictType> reqTypes) {
        return parallelDistrictService.assignDistricts(this, geocodedAddresses, reqTypes);
    }

    /** Assignment method to be called when using ParallelDistrictService.
     *  May simply just be a delegate to assignDistricts depending on the implementation. */
    public DistrictResult assignDistrictsForBatch(GeocodedAddress geocodedAddress, List<DistrictType> reqTypes) {
        return assignDistricts(geocodedAddress, reqTypes);
    }

    public Map<String, DistrictMap> nearbyDistricts(GeocodedAddress geocodedAddress, DistrictType districtType) {
        return nearbyDistricts(geocodedAddress, districtType, 0);
    }

    public Map<String, DistrictMap> nearbyDistricts(GeocodedAddress geocodedAddress, DistrictType districtType, int count) {
        logger.warn("{} does not implement nearbyDistricts.", getClass());
        return null;
    }
}
