package gov.nysenate.sage.model.result;

import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.provider.district.MapSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents resulting district map information from map providers.
 */
public class MapResult extends BaseResult<MapSource> {
    protected List<DistrictMap> districtMaps = new ArrayList<>();

    public MapResult(MapSource source) {
        super(source);
    }

    public DistrictMap getDistrictMap() {
        if (!districtMaps.isEmpty()) {
            return districtMaps.get(0);
        }
        else {
            return null;
        }
    }

    public List<DistrictMap> getDistrictMaps() {
        return districtMaps;
    }

    public void setDistrictMaps(List<DistrictMap> districtMaps) {
        this.districtMaps = districtMaps;
    }

    public void setDistrictMap(DistrictMap districtMap) {
        this.districtMaps.add(districtMap);
    }
}
