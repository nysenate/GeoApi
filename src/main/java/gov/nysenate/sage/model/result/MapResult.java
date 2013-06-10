package gov.nysenate.sage.model.result;

import gov.nysenate.sage.model.district.DistrictMap;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents resulting district map information from map providers.
 */
public class MapResult extends BaseResult
{
    protected List<DistrictMap> districtMaps = new ArrayList<>();

    public MapResult() {
        this(null, null);
    }

    public MapResult(Class sourceClass) {
        this(sourceClass, null);
    }

    public MapResult(Class sourceClass, ResultStatus resultStatus) {
        this.setSource(sourceClass);
        this.setStatusCode(resultStatus);
    }

    public DistrictMap getDistrictMap() {
        if (this.districtMaps.size() > 0) {
            return this.districtMaps.get(0);
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
