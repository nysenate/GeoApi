package gov.nysenate.sage.model.result;

import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictOverlap;
import gov.nysenate.sage.provider.district.MapSource;

public class IntersectResult extends BaseResult<MapSource> {
    private final DistrictMap mainMap;
    private final DistrictOverlap overlap;

    public IntersectResult(MapSource source, DistrictMap mainMap, DistrictOverlap overlap) {
        super(source);
        this.mainMap = mainMap;
        this.overlap = overlap;
    }

    public DistrictMap getMainMap() {
        return mainMap;
    }

    public DistrictOverlap getOverlap() {
        return overlap;
    }
}
