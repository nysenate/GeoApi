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

    /** A set of the DistrictType's that were actually district assigned */
    protected Set<DistrictType> assignedDistricts;

    public DistrictResult()
    {
        this(null, new HashSet<DistrictType>());
    }

    public DistrictResult(DistrictedAddress districtedAddress)
    {
        this(districtedAddress, new HashSet<DistrictType>());
    }

    public DistrictResult(DistrictedAddress districtedAddress, Set<DistrictType> assignedDistricts )
    {
        this.districtedAddress = districtedAddress;
        this.assignedDistricts = assignedDistricts;
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
