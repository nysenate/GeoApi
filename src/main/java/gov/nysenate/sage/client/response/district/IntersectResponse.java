package gov.nysenate.sage.client.response.district;

import com.fasterxml.jackson.annotation.JsonRawValue;
import gov.nysenate.sage.client.view.district.DistrictOverlapView;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.IntersectResult;

import java.math.BigDecimal;
import java.util.List;

public record IntersectResponse(List<DistrictOverlapView> overlaps, DistrictType intersectType,
                                String intersectTypeDisplayName, @JsonRawValue String referenceMap,
                                BigDecimal totalReferenceArea, String areaUnit) {
    // Due to imperfect accuracy, sometimes false overlaps are returned for bordering districts.
    private static final BigDecimal MIN_AREA_PERCENTAGE = new BigDecimal("0.05");

    public static IntersectResponse from(IntersectResult intersectResult) {
        BigDecimal totalArea = intersectResult.getMainMap().getArea();
        List<DistrictOverlapView> overlaps = intersectResult.getOverlaps().stream()
                .map(dMap -> new DistrictOverlapView(dMap, totalArea))
                .filter(dov -> dov.getAreaPercentage().compareTo(MIN_AREA_PERCENTAGE) >= 0).toList();
        return new IntersectResponse(overlaps, intersectResult.getIntersectType(),
                intersectResult.getIntersectType().getDisplayName(), intersectResult.getMainMap().getMapGeoJson(),
                intersectResult.getMainMap().getArea(), "SQ_KILOMETERS");
    }
}
