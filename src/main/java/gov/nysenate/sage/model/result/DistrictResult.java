package gov.nysenate.sage.model.result;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.DistrictedAddress;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictMatchLevel;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;
import gov.nysenate.sage.provider.district.LocalSource;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static gov.nysenate.sage.model.result.ResultStatus.*;

/**
 * Represents the result returned by district assignment services.
 */
public class DistrictResult extends BaseResult<LocalSource> {
    /** We should only attempt to assign districts to a geocode if it is accurate enough. */
    private static final List<GeocodeQuality> DISTRICT_ASSIGNABLE_GEOCODE_QUALITIES =
            List.of(GeocodeQuality.HOUSE, GeocodeQuality.POINT);

    /** Contains the geocoded address and district information */
    @Nonnull
    private DistrictedAddress districtedAddress;

    public DistrictResult(LocalSource source, GeocodedAddress geoAddress) {
        this(source, geoAddress, getStatus(geoAddress, source));
    }

    public DistrictResult(LocalSource source, GeocodedAddress geoAddress, ResultStatus statusCode) {
        super(source);
        this.districtedAddress = new DistrictedAddress(geoAddress, null);
        this.statusCode = statusCode;
    }

    public DistrictInfo getDistrictInfo() {
        return districtedAddress.getDistrictInfo();
    }

    public void setDistrictInfo(DistrictInfo districtInfo) {
        this.districtedAddress.setDistrictInfo(districtInfo);
    }

    public Geocode getGeocode() {
        return districtedAddress.getGeocode();
    }

    public Address getAddress() {
        return districtedAddress.getAddress();
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
        }
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
        return isSuccess() && getDistrictInfo().getMatchLevel().compareTo(DistrictMatchLevel.HOUSE) < 0;
    }

    private static ResultStatus getStatus(final GeocodedAddress geoAddress, LocalSource source) {
        if (geoAddress == null) {
            return MISSING_GEOCODED_ADDRESS;
        }
        if (!geoAddress.isValidAddress() && source == LocalSource.STREETFILE) {
            return INVALID_ADDRESS;
        }
        else if (source == LocalSource.SHAPEFILE) {
            if (!geoAddress.isValidGeocode()) {
                return INVALID_GEOCODE;
            }
            if (!DISTRICT_ASSIGNABLE_GEOCODE_QUALITIES.contains(geoAddress.getGeocode().quality())) {
                return INSUFFICIENT_GEOCODE;
            }
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
