package gov.nysenate.sage.model.result;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.DistrictedAddress;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictMatchLevel;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.geo.Geocode;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents the result returned by district assignment services.
 */
public class DistrictResult extends BaseResult
{
    /** Contains the geocoded address and district information */
    protected DistrictedAddress districtedAddress;

    public DistrictResult()
    {
        this(null, null);
    }

    public DistrictResult(Class sourceClass)
    {
        this(sourceClass, null);
    }

    public DistrictResult(Class sourceClass, DistrictedAddress districtedAddress)
    {
        this.setSource(sourceClass);
        this.setDistrictedAddress(districtedAddress);
    }

    public DistrictInfo getDistrictInfo()
    {
        return (districtedAddress != null) ? districtedAddress.getDistrictInfo() : null;
    }

    public void setDistrictInfo(DistrictInfo districtInfo)
    {
        if (this.districtedAddress == null) {
            this.districtedAddress = new DistrictedAddress(null, districtInfo);
        }
        else {
            this.districtedAddress.setDistrictInfo(districtInfo);
        }
    }

    public Geocode getGeocode()
    {
        return (districtedAddress != null) ? districtedAddress.getGeocode() : null;
    }

    public Address getAddress()
    {
        return (districtedAddress != null) ? districtedAddress.getAddress() : null;
    }

    public void setGeocodedAddress(GeocodedAddress geocodedAddress)
    {
        if (this.districtedAddress == null) {
            this.districtedAddress = new DistrictedAddress();
        }
        this.districtedAddress.setGeocodedAddress(geocodedAddress);
    }

    public GeocodedAddress getGeocodedAddress()
    {
        return (districtedAddress != null) ? districtedAddress.getGeocodedAddress() : null;
    }

    public DistrictedAddress getDistrictedAddress()
    {
        return districtedAddress;
    }

    public void setDistrictedAddress(DistrictedAddress districtedAddress)
    {
        this.districtedAddress = districtedAddress;
    }

    public DistrictMatchLevel getDistrictMatchLevel() {
        return (this.districtedAddress != null) ? this.districtedAddress.getDistrictMatchLevel()
                                                : DistrictMatchLevel.NOMATCH;
    }

    public void setDistrictMatchLevel(DistrictMatchLevel quality) {
        if (this.districtedAddress == null) {
            this.districtedAddress = new DistrictedAddress();
        }
        this.districtedAddress.setDistrictMatchLevel(quality);
    }

    /** Accessor method to the set of assigned districts stored in DistrictInfo */
    public Set<DistrictType> getAssignedDistricts()
    {
        return (this.getDistrictInfo() != null) ? this.getDistrictInfo().getAssignedDistricts()
                                                : new HashSet<DistrictType>();
    }

    /**
     * Determines if result has a multi district overlap condition.
     * @return true if multi match, false otherwise
     */
    public boolean isMultiMatch() {
        return (this.isSuccess() && this.getDistrictMatchLevel().compareTo(DistrictMatchLevel.HOUSE) < 0);
    }

    public boolean isUspsValidated() {
        return (getAddress() != null) ? getAddress().isUspsValidated() : false;
    }
}
