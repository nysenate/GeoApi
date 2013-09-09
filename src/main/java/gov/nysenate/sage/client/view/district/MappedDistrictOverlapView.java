package gov.nysenate.sage.client.view.district;

import gov.nysenate.sage.client.view.map.MapView;
import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictMatchLevel;
import gov.nysenate.sage.model.district.DistrictOverlap;

public class MappedDistrictOverlapView extends DistrictOverlapView
{
    protected MapView map;
    protected MapView fullMap;
    protected Object member;

    public MappedDistrictOverlapView(DistrictOverlap districtOverlap, String district, DistrictMatchLevel matchLevel)
    {
        super(districtOverlap, district, matchLevel);
        if (districtOverlap != null && district != null) {
            DistrictMap intersectionMap = districtOverlap.getIntersectionMap(district);
            DistrictMap districtMap = districtOverlap.getTargetDistrictMap(district);

            if (intersectionMap != null && !matchLevel.equals(DistrictMatchLevel.STREET)) {
                this.map = new MapView(intersectionMap);
                if (districtMap != null) {
                    this.fullMap = new MapView(districtMap);
                }
            }
            else if (districtMap != null) {
                this.map = new MapView(districtMap);
            }

            if (!districtOverlap.getTargetSenators().isEmpty()) {
                this.member = districtOverlap.getTargetSenators().get(district);
            }
        }
    }

    public MapView getMap() {
        return map;
    }

    public Object getMember() {
        return member;
    }

    public MapView getFullMap() {
        return fullMap;
    }
}