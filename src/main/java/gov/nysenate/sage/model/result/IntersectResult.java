package gov.nysenate.sage.model.result;

import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.provider.district.MapSource;

import java.util.List;

public class IntersectResult extends BaseResult<MapSource> {
    private final DistrictMap mainMap;
    private final DistrictType intersectType;
    private final List<DistrictMap> overlap;

    public IntersectResult(MapSource source, DistrictMap mainMap, DistrictType intersectType,
                           List<DistrictMap> overlap) {
        super(source);
        this.mainMap = mainMap;
        this.intersectType = intersectType;
        this.overlap = overlap;
    }

    public DistrictMap getMainMap() {
        return mainMap;
    }

    public DistrictType getIntersectType() {
        return intersectType;
    }

    public List<DistrictMap> getOverlap() {
        return overlap;
    }
}
