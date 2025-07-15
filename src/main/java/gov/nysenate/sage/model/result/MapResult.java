package gov.nysenate.sage.model.result;

import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.provider.district.LocalSource;

/**
 * Represents resulting district map information from map providers.
 */
public class MapResult extends BaseResult<LocalSource> {
    private final DistrictMap districtMap;

    public MapResult(ResultStatus statusCode) {
        super(LocalSource.SHAPEFILE);
        this.districtMap = null;
        setStatusCode(statusCode);
    }

    public MapResult(DistrictMap districtMap) {
        super(LocalSource.SHAPEFILE);
        this.districtMap = districtMap;
    }

    public DistrictMap getDistrictMap() {
        return districtMap;
    }
}
