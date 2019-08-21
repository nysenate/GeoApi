package gov.nysenate.sage.service.district;

import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.model.result.MapResult;

public interface SageDistrictMemberProvider {

    /**
     * Sets the senator, congressional, and assembly member data to the district result.
     * @param districtResult
     */
    public void assignDistrictMembers(DistrictResult districtResult);

    /**
     * Sets the senator, congressional, and assembly member data to the map result.
     * @param mapResult
     */
    public void assignDistrictMembers(MapResult mapResult);
}
