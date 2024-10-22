package gov.nysenate.sage.service.district;

import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.model.result.MapResult;

public interface SageDistrictMemberProvider {
    /**
     * Sets the senator, congressional, and assembly member data to the district result.
     */
    void assignDistrictMembers(DistrictResult districtResult);

    /**
     * Sets the senator, congressional, and assembly member data to the map result.
     */
    void assignDistrictMembers(MapResult mapResult);
}
