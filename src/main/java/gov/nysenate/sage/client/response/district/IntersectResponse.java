package gov.nysenate.sage.client.response.district;

import gov.nysenate.sage.client.view.district.MappedDistrictOverlapView;
import gov.nysenate.sage.client.view.map.PolygonMapView;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.IntersectResult;

import java.math.BigDecimal;

public record IntersectResponse(MappedDistrictOverlapView overlap, String intersectType,
                                PolygonMapView referenceMap, BigDecimal totalReferenceArea, String areaUnit) {
    public IntersectResponse(IntersectResult intersectResult, DistrictType intersectType) {
        this(null,
                // TODO
                //new MappedDistrictOverlapView(intersectResult.getOverlap(), intersectResult.getMainMap().getDistrictCode()),
                intersectType.name().toLowerCase(), new PolygonMapView(intersectResult.getMainMap()),
                intersectResult.getOverlap().getTotalArea(), intersectResult.getOverlap().getAreaUnit());
    }
}
