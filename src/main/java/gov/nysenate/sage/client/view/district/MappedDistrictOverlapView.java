package gov.nysenate.sage.client.view.district;

import gov.nysenate.sage.client.view.map.MapView;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictOverlap;
import gov.nysenate.sage.model.district.DistrictType;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MappedDistrictOverlapView
{
    protected String name;
    protected String district;
    protected MapView map;
    protected BigDecimal intersectionArea;
    protected BigDecimal areaPercentage;

    public MappedDistrictOverlapView(DistrictOverlap districtOverlap, String district)
    {
        if (districtOverlap != null && district != null) {
            this.district = district;
            DistrictMap intersectionMap = districtOverlap.getIntersectionMap(district);
            DistrictMap districtMap = districtOverlap.getTargetDistrictMap(district);
            if (intersectionMap != null) {
                this.map = new MapView(intersectionMap);
            }
            else if (districtMap != null) {
                this.name = districtMap.getDistrictName();
                this.map = new MapView(districtMap);
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

    public BigDecimal getIntersectionArea() {
        return intersectionArea;
    }

    public BigDecimal getAreaPercentage() {
        return areaPercentage;
    }

    public void setAreaPercentage(BigDecimal areaPercentage) {
        this.areaPercentage = areaPercentage;
    }
}