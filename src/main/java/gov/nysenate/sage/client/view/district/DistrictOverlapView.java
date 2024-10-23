package gov.nysenate.sage.client.view.district;

import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictOverlap;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class DistrictOverlapView {
    protected String name;
    protected String district;
    protected BigDecimal intersectionArea;
    protected BigDecimal areaPercentage;

    public DistrictOverlapView(DistrictOverlap districtOverlap, String district) {
        if (districtOverlap != null && district != null) {
            DistrictMap districtMap = districtOverlap.getTargetDistrictMap(district);
            BigDecimal totalArea = districtOverlap.getTotalArea();

            this.district = district;
            if (districtMap != null) {
                this.name = districtMap.getDistrictName();
            }
            this.intersectionArea = districtOverlap.getTargetOverlap(district);
            this.areaPercentage = this.intersectionArea.divide(totalArea, 2, RoundingMode.HALF_UP);
        }
    }

    public String getName() {
        return name;
    }

    public String getDistrict() {
        return district;
    }

    public BigDecimal getIntersectionArea() {
        return intersectionArea;
    }

    public BigDecimal getAreaPercentage() {
        return areaPercentage;
    }
}
