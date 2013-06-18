package gov.nysenate.sage.model.result;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.DistrictedAddress;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.services.model.Senator;

import java.util.*;

/**
 * Represents the result returned by district assignment services.
 */
public class DistrictResult extends BaseResult
{
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

    /** Accessor method to the set of assigned districts stored in DistrictInfo */
    public Set<DistrictType> getAssignedDistricts()
    {
        if (this.getDistrictInfo() != null){
            return this.getDistrictInfo().getAssignedDistricts();
        }
        return new HashSet<>();
    }

    public boolean isPartialSuccess() {
        return (this.statusCode != null && this.statusCode.equals(ResultStatus.PARTIAL_DISTRICT_RESULT));
    }
}
