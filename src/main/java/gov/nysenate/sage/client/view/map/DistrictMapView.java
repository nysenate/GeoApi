package gov.nysenate.sage.client.view.map;

import com.fasterxml.jackson.annotation.JsonRawValue;
import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictMember;
import gov.nysenate.sage.model.district.DistrictType;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class DistrictMapView {
    private DistrictType type;
    private String district;
    private String name;
    // Raw GeoJSON geometry, emitted directly to the client.
    @JsonRawValue
    private String map;
    private DistrictMember member;
    private String link;
    private BigDecimal area;

    public DistrictMapView(DistrictMap districtMap, boolean showMaps) {
        if (districtMap != null) {
            this.type = districtMap.getDistrictType();
            this.district = districtMap.getDistrictCode();
            this.name = districtMap.getDistrictName();
            this.map = (showMaps) ? districtMap.getMapGeoJson() : null;
            this.member = districtMap.getMember();
            if (type == DistrictType.COUNTY) {
                this.link = districtMap.getLink();
            }
            this.area = districtMap.getArea();
        }
    }
}
