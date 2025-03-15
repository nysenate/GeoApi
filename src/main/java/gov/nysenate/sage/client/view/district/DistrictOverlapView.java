package gov.nysenate.sage.client.view.district;

import gov.nysenate.sage.client.view.map.DistrictMapView;
import gov.nysenate.sage.model.district.DistrictMap;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class DistrictOverlapView extends DistrictMapView {
    protected BigDecimal areaPercentage;

    public DistrictOverlapView(@Nonnull DistrictMap intersectionMap, @Nonnull BigDecimal baseArea) {
        super(intersectionMap, true);
        this.areaPercentage = new BigDecimal(100).multiply(area).divide(baseArea, 2, RoundingMode.HALF_UP);
    }
}
