package gov.nysenate.sage.model.result;

import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.provider.district.LocalSource;

/**
 * Represents resulting district map information from map providers.
 */
public class MapResult extends BaseResult<LocalSource> {
    private DistrictMap districtMap = null;

    public MapResult() {
        super(LocalSource.SHAPEFILE);
    }

    public DistrictMap getDistrictMap() {
        return districtMap;
    }

    public void setDistrictMap(DistrictMap districtMap) {
        this.districtMap = districtMap;
    }
}
