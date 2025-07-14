package gov.nysenate.sage.client.view.district;

import gov.nysenate.sage.client.view.map.DistrictMapView;
import gov.nysenate.sage.client.view.map.PolygonMapView;
import gov.nysenate.sage.model.district.IntersectMap;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class DistrictOverlapView extends DistrictMapView {
    private final BigDecimal areaPercentage;
    private final PolygonMapView fullMap;

    public DistrictOverlapView(@Nonnull IntersectMap intersectionMap, @Nonnull BigDecimal baseArea) {
        super(intersectionMap, true);
        this.areaPercentage = new BigDecimal(100).multiply(area).divide(baseArea, 2, RoundingMode.HALF_UP);
        this.fullMap = new PolygonMapView(intersectionMap.getFullMapPolygons(), intersectionMap.getGeometryType());
    }

    public BigDecimal getAreaPercentage() {
        return areaPercentage;
    }

    public PolygonMapView getFullMap() {
        return fullMap;
    }
}
