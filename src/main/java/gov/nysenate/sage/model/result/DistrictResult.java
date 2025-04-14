package gov.nysenate.sage.model.result;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictMatchLevel;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.provider.district.LocalSource;

import java.util.Set;

import static gov.nysenate.sage.model.geo.GeocodeQuality.HOUSE;
import static gov.nysenate.sage.model.geo.GeocodeQuality.POINT;
import static gov.nysenate.sage.model.result.ResultStatus.*;

/**
 * Represents the result returned by the district assignment service.
 */
public class DistrictResult extends BaseResult<LocalSource> {
    private final GeocodedAddress geoAddress;
    private DistrictInfo districtInfo;

    public DistrictResult(LocalSource source, GeocodedAddress geoAddress, DistrictInfo districtInfo) {
        super(source);
        this.statusCode = getStatus(geoAddress, source);
        this.geoAddress = geoAddress;
        this.districtInfo = districtInfo;
        setResultTime();
    }

    public DistrictResult(LocalSource source, GeocodedAddress geoAddress) {
        this(source, geoAddress, DistrictInfo.empty);
    }

    public GeocodedAddress getGeoAddress() {
        return geoAddress;
    }

    public DistrictInfo getDistrictInfo() {
        return districtInfo;
    }

    public void setDistrictInfo(DistrictInfo districtInfo) {
        this.districtInfo = districtInfo;
    }

    public Geocode getGeocode() {
        return geoAddress == null ? null : geoAddress.getGeocode();
    }

    public Address getAddress() {
        return geoAddress == null ? null : geoAddress.getAddress();
    }

    /** Accessor method to the set of assigned districts stored in DistrictInfo */
    public Set<DistrictType> getAssignedDistricts() {
        return getDistrictInfo() == null ? Set.of() : getDistrictInfo().typeToDistrictMap().keySet();
    }

    /**
     * Determines if result has a multi-district overlap condition.
     * @return true if multi match, false otherwise
     */
    public boolean isMultiMatch() {
        return isSuccess() && getDistrictInfo().matchLevel().compareTo(DistrictMatchLevel.HOUSE) < 0;
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
            if (geoAddress.getGeocode().quality() != HOUSE && geoAddress.getGeocode().quality() != POINT) {
                return INSUFFICIENT_GEOCODE;
            }
        }
        else if (geoAddress.isValidAddress()) {
            if (geoAddress.getAddress().isOutOfState()) {
                return NON_NY_STATE;
            }
        }
        return SUCCESS;
    }
}
