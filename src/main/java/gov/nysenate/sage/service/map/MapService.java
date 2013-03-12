package gov.nysenate.sage.service.map;

import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictType;

public interface MapService
{
    public DistrictMap getDistrictMap(DistrictType districtType, String code);
}
