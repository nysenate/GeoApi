package gov.nysenate.sage.client.view.district;

import gov.nysenate.sage.client.view.map.MapView;
import gov.nysenate.sage.client.view.street.StreetRangeView;
import gov.nysenate.sage.model.district.*;
import gov.nysenate.sage.model.geo.Line;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MappedDistrictOverlapView
{
    protected String name;
    protected String district;
    protected MapView map;
    protected Object member;
    protected BigDecimal intersectionArea;
    protected BigDecimal areaPercentage;

    public MappedDistrictOverlapView(DistrictOverlap districtOverlap, String district, DistrictMatchLevel matchLevel)
    {
        if (districtOverlap != null && district != null) {
            this.district = district;
            DistrictMap intersectionMap = districtOverlap.getIntersectionMap(district);
            DistrictMap districtMap = districtOverlap.getTargetDistrictMap(district);

            if (intersectionMap != null && !matchLevel.equals(DistrictMatchLevel.STREET)) {
                this.map = new MapView(intersectionMap);
            }
            else if (districtMap != null) {
                this.name = districtMap.getDistrictName();
                this.map = new MapView(districtMap);
            }

            if (!districtOverlap.getTargetSenators().isEmpty()) {
                this.member = districtOverlap.getTargetSenators().get(district);
            }

            this.intersectionArea = districtOverlap.getTargetOverlap(district);
            BigDecimal totalArea = districtOverlap.getTotalArea();
            areaPercentage = this.intersectionArea.divide(totalArea, 2, RoundingMode.HALF_UP);
        }
    }

    public String getName() {
        return name;
    }

    public String getDistrict() {
        return district;
    }

    public MapView getMap() {
        return map;
    }

    public Object getMember() {
        return member;
    }

    public BigDecimal getIntersectionArea() {
        return intersectionArea;
    }

    public BigDecimal getAreaPercentage() {
        return areaPercentage;
    }
}