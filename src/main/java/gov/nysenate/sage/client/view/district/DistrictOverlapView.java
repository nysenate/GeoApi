package gov.nysenate.sage.client.view.district;

import gov.nysenate.sage.model.district.DistrictMap;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class DistrictOverlapView {
    protected String name;
    protected String district;
    protected BigDecimal intersectionArea;
    protected BigDecimal areaPercentage;

    public DistrictOverlapView(@Nonnull DistrictMap baseMap, @Nonnull DistrictMap intersectionMap) {
        this.name = intersectionMap.getDistrictName();
        this.district = intersectionMap.getDistrictCode();
        this.intersectionArea = intersectionMap.getArea();
        this.areaPercentage = intersectionArea.divide(baseMap.getArea(), 2, RoundingMode.HALF_UP);
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
