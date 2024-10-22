package gov.nysenate.sage.provider.district;

import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.MapResult;

/**
 * MapService is used to return district maps for state level districts.
 * Only state level districts (senate, assembly, congressional, county) are
 * supported because lower level districts are not uniquely identified by a code.
 */
public interface MapService {
    /** Provides a district map given a specific district */
    MapResult getDistrictMap(DistrictType districtType, String code);

    /** Provides a collection of all district maps for a given type */
    MapResult getDistrictMaps(DistrictType districtType);
}
