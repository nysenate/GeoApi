package gov.nysenate.sage.model.result;

import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.provider.district.MapSource;

import java.util.List;

public class IntersectResult extends BaseResult<MapSource> {
    private final DistrictMap mainMap;
    private final List<DistrictMap> overlap;

    public IntersectResult(MapSource source, DistrictMap mainMap, List<DistrictMap> overlap) {
        super(source);
        this.mainMap = mainMap;
        this.overlap = overlap;
    }

    public DistrictMap getMainMap() {
        return mainMap;
    }

    public List<DistrictMap> getOverlap() {
        return overlap;
    }
}
