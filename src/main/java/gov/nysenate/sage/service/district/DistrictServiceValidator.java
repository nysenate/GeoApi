package gov.nysenate.sage.service.district;

import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.result.ResultStatus;

import static gov.nysenate.sage.model.result.ResultStatus.*;

/**
 * Simple validation methods that can be utilized by provider implementations of DistrictService
 */
public class DistrictServiceValidator {
    public static ResultStatus getStatus(final GeocodedAddress geoAddress, boolean requireGeocode, boolean requiresValidAddress) {
        if (geoAddress == null) {
            return MISSING_GEOCODED_ADDRESS;
        }
        if (!geoAddress.isValidAddress() && requiresValidAddress) {
            return MISSING_ADDRESS;
        }
        else if (!geoAddress.isValidGeocode() && requireGeocode) {
            return MISSING_GEOCODE;
        }
        else if (geoAddress.isValidAddress()) {
            String state = geoAddress.getAddress().getState();
            if (state != null && !state.isEmpty() && !state.matches("(?i)(NY|NEW YORK)")) {
                return NON_NY_STATE;
            }
        }
        return SUCCESS;
    }
}
