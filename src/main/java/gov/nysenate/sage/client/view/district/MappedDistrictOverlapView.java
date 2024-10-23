package gov.nysenate.sage.client.view.district;

import gov.nysenate.sage.client.view.map.PolygonMapView;
import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictMatchLevel;
import gov.nysenate.sage.model.district.DistrictOverlap;

public class MappedDistrictOverlapView extends DistrictOverlapView {
    protected PolygonMapView map;
    protected PolygonMapView fullMap;
    protected Object member;

    public MappedDistrictOverlapView(DistrictOverlap districtOverlap, String district, DistrictMatchLevel matchLevel) {
        super(districtOverlap, district);
        if (districtOverlap != null && district != null) {
            DistrictMap intersectionMap = districtOverlap.getIntersectionMap(district);
            DistrictMap districtMap = districtOverlap.getTargetDistrictMap(district);

            if (intersectionMap != null && !matchLevel.equals(DistrictMatchLevel.STREET)) {
                this.map = new PolygonMapView(intersectionMap);
                if (districtMap != null) {
                    this.fullMap = new PolygonMapView(districtMap);
                }
            }
            else if (districtMap != null) {
                this.map = new PolygonMapView(districtMap);
            }

            if (!districtOverlap.getTargetSenators().isEmpty()) {
                this.member = districtOverlap.getTargetSenators().get(district);
            }
        }
    }

    public PolygonMapView getMap() {
        return map;
    }

    public Object getMember() {
        return member;
    }

    public PolygonMapView getFullMap() {
        return fullMap;
    }
}