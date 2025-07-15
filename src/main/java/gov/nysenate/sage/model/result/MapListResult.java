package gov.nysenate.sage.model.result;

import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.provider.district.LocalSource;

import java.util.SortedSet;

public class MapListResult extends BaseResult<LocalSource> {
    private final SortedSet<DistrictMap> districtMaps;

    public MapListResult(ResultStatus statusCode) {
        super(LocalSource.SHAPEFILE);
        setStatusCode(statusCode);
        this.districtMaps = null;
    }

    public MapListResult(SortedSet<DistrictMap> districtMaps) {
        super(LocalSource.SHAPEFILE);
        this.districtMaps = districtMaps;
    }

    public SortedSet<DistrictMap> getDistrictMaps() {
        return districtMaps;
    }
}
