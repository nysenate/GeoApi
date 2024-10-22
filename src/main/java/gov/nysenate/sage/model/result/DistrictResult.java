package gov.nysenate.sage.model.result;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.DistrictedAddress;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictMatchLevel;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.provider.district.DistrictSource;
import gov.nysenate.sage.service.district.DistrictServiceValidator;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents the result returned by district assignment services.
 */
public class DistrictResult extends BaseResult<DistrictSource> {
    /** Contains the geocoded address and district information */
    @Nonnull
    private DistrictedAddress districtedAddress;

    public DistrictResult(DistrictSource source, GeocodedAddress geoAddress,
                          boolean requireGeocode, boolean requiresValidAddress) {
        this(source, geoAddress, DistrictServiceValidator.getStatus(geoAddress, requireGeocode, requiresValidAddress));
    }

    public DistrictResult(DistrictSource source, GeocodedAddress geoAddress, ResultStatus statusCode) {
        super(source);
        this.districtedAddress = new DistrictedAddress(geoAddress, null);
        this.statusCode = statusCode;
    }

    public DistrictInfo getDistrictInfo() {
        return districtedAddress.getDistrictInfo();
    }

    public Geocode getGeocode() {
        return districtedAddress.getGeocode();
    }

    public Address getAddress() {
        return districtedAddress.getAddress();
    }

    public GeocodedAddress getGeocodedAddress() {
        return districtedAddress.getGeocodedAddress();
    }

    @Nonnull
    public DistrictedAddress getDistrictedAddress() {
        return districtedAddress;
    }

    public void setDistrictedAddress(DistrictedAddress districtedAddress) {
        if (districtedAddress == null) {
            districtedAddress = new DistrictedAddress();
        }
        this.districtedAddress = districtedAddress;
        if (getDistrictInfo() == null || getDistrictInfo().getAssignedDistricts().isEmpty()) {
            this.statusCode = ResultStatus.NO_DISTRICT_RESULT;
            districtedAddress.setDistrictMatchLevel(DistrictMatchLevel.NOMATCH);
        }
    }

    public DistrictMatchLevel getDistrictMatchLevel() {
        return this.districtedAddress.getDistrictMatchLevel();
    }

    /** Accessor method to the set of assigned districts stored in DistrictInfo */
    public Set<DistrictType> getAssignedDistricts() {
        return (this.getDistrictInfo() != null) ? this.getDistrictInfo().getAssignedDistricts()
                                                : new HashSet<>();
    }

    /**
     * Determines if result has a multi-district overlap condition.
     * @return true if multi match, false otherwise
     */
    public boolean isMultiMatch() {
        return isSuccess() && getDistrictMatchLevel().compareTo(DistrictMatchLevel.HOUSE) < 0;
    }

    public boolean isUspsValidated() {
        return getAddress() != null && getAddress().isUspsValidated();
    }
}
