package gov.nysenate.sage.client.view.district;

import com.fasterxml.jackson.annotation.JsonRawValue;
import gov.nysenate.sage.client.view.map.DistrictMapView;
import gov.nysenate.sage.model.district.IntersectMap;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class DistrictOverlapView extends DistrictMapView {
    private final BigDecimal areaPercentage;
    // Raw GeoJSON geometry for the full (un-clipped) intersecting district.
    private final String fullMap;

    public DistrictOverlapView(@Nonnull IntersectMap intersectionMap, @Nonnull BigDecimal baseArea) {
        super(intersectionMap, true);
        this.areaPercentage = new BigDecimal(100).multiply(getArea()).divide(baseArea, 2, RoundingMode.HALF_UP);
        this.fullMap = intersectionMap.getFullMapGeoJson();
    }

    public BigDecimal getAreaPercentage() {
        return areaPercentage;
    }

    @JsonRawValue
    public String getFullMap() {
        return fullMap;
    }
}
