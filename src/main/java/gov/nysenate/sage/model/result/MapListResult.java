package gov.nysenate.sage.model.result;

import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.provider.district.LocalSource;

import java.util.List;

public class MapListResult extends BaseResult<LocalSource> {
    private List<DistrictMap> districtMaps = null;

    public MapListResult() {
        super(LocalSource.SHAPEFILE);
    }

    public List<DistrictMap> getDistrictMaps() {
        return districtMaps;
    }

    public void setDistrictMaps(List<DistrictMap> districtMaps) {
        this.districtMaps = districtMaps;
    }
}
