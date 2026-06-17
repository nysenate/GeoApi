package gov.nysenate.sage.client.view.map;

import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictMember;
import gov.nysenate.sage.model.district.DistrictType;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class DistrictMapView {
    private String type;
    private String district;
    private String name;
    private PolygonMapView map;
    private DistrictMember member;
    private String link;
    private BigDecimal area;

    public DistrictMapView(DistrictMap districtMap, boolean showMaps) {
        if (districtMap != null) {
            DistrictType districtType = districtMap.getDistrictType();
            if (districtType != null) {
                this.type = districtType.name();
            }
            this.district = districtMap.getDistrictCode();
            this.name = districtMap.getDistrictName();
            this.map = (showMaps) ? new PolygonMapView(districtMap) : null;
            this.member = districtMap.getMember();
            if (districtType == DistrictType.COUNTY) {
                this.link = districtMap.getLink();
            }
            this.area = districtMap.getArea();
        }
    }
}
