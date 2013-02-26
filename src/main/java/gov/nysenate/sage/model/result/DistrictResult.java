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

    public void setDistrictedAddress(DistrictedAddress districtedAddress)
    {
        this.districtedAddress = districtedAddress;
    }

    public Set<DistrictType> getAssignedDistricts()
    {
        return this.assignedDistricts;
    }

    public void setAssignedDistricts(Set<DistrictType> assignedDistricts)
    {
        this.assignedDistricts = assignedDistricts;
    }

    public void addAssignedDistrict(DistrictType district)
    {
        this.assignedDistricts.add(district);
    }




}
