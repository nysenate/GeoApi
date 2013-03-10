package gov.nysenate.sage.model.result;

import gov.nysenate.sage.model.address.DistrictedAddress;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictType;

import java.util.*;

/**
 * Represents the result returned by district assignment services.
 */
public class DistrictResult extends BaseResult
{
    /** The districted address */
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

    public DistrictedAddress getDistrictedAddress()
    {
        return districtedAddress;
    }

    /** Accessor method to the set of assigned districts stored in DistrictInfo */
    public Set<DistrictType> getAssignedDistricts()
    {
        if (this.getDistrictInfo() != null){
            return this.getDistrictInfo().getAssignedDistricts();
        }
        return null;
    }

    public void setDistrictedAddress(DistrictedAddress districtedAddress)
    {
        this.districtedAddress = districtedAddress;
    }
}
