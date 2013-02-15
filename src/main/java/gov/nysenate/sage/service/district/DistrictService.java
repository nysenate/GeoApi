package gov.nysenate.sage.service.district;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.result.DistrictResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Comment this later..
 */
public interface DistrictService
{
    public DistrictService newInstance();

    public DistrictResult assignDistrict(Address address);
    public ArrayList<DistrictResult> assignDistrict(ArrayList<Address> addresses);
    public ArrayList<DistrictResult> assignDistricts(ArrayList<Address> addresses);
    public DistrictResult assignDistricts(Address address);
}
