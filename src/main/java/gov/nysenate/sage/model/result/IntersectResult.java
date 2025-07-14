package gov.nysenate.sage.model.result;

import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.district.IntersectMap;
import gov.nysenate.sage.provider.district.LocalSource;

import java.util.List;

public class IntersectResult extends BaseResult<LocalSource> {
    private final DistrictMap mainMap;
    private final DistrictType intersectType;
    private final List<IntersectMap> overlaps;

    public IntersectResult(DistrictMap mainMap, DistrictType intersectType,
                           List<IntersectMap> overlaps) {
        super(LocalSource.SHAPEFILE);
        this.mainMap = mainMap;
        this.intersectType = intersectType;
        this.overlaps = overlaps;
    }

    public DistrictMap getMainMap() {
        return mainMap;
    }

    public DistrictType getIntersectType() {
        return intersectType;
    }

    public List<IntersectMap> getOverlaps() {
        return overlaps;
    }
}
