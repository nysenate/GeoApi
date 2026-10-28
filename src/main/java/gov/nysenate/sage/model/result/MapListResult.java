package gov.nysenate.sage.model.result;

import gov.nysenate.sage.model.district.DistrictId;
import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.provider.district.LocalSource;
import lombok.Getter;

import java.util.Map;

@Getter
public class MapListResult extends BaseResult<LocalSource> {
    private final Map<DistrictId, DistrictMap> districtMaps;

    public MapListResult(Map<DistrictId, DistrictMap> districtMaps) {
        super(LocalSource.SHAPEFILE);
        this.districtMaps = districtMaps;
    }
}
