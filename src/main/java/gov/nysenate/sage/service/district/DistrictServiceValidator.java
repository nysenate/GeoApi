package gov.nysenate.sage.service.district;

import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.result.DistrictResult;

import static gov.nysenate.sage.model.result.ResultStatus.*;

/**
 * Simple validation methods that can be utilized by provider implementations of DistrictService
 */
public class DistrictServiceValidator
{
    /**
     * Perform basic null checks on the input parameters.
     * @return true if all required objects are set, false otherwise and sets status in districtResult
     */
    public static boolean validate(final GeocodedAddress geoAddress, final DistrictResult districtResult,
                                   final boolean requireGeocode)
    {
        if (geoAddress == null) {
            districtResult.setStatusCode(MISSING_INPUT_PARAMS);
        }
        else
        {
            if (geoAddress.getAddress() == null) {
                districtResult.setStatusCode(MISSING_ADDRESS);
            }
            else if (geoAddress.getAddress().isEmpty()) {
                districtResult.setStatusCode(INSUFFICIENT_ADDRESS);
            }
            else if (geoAddress.getGeocode() == null && requireGeocode) {
                districtResult.setStatusCode(MISSING_GEOCODE);
            }
            else if (geoAddress.getGeocode().getLatLon() == null && requireGeocode)
            {
                districtResult.setStatusCode(INVALID_GEOCODE);
            }
            else {
                return true;
            }
        }
        return false;
    }
}
