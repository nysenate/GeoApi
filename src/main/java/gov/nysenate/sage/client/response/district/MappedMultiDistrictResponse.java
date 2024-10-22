package gov.nysenate.sage.client.response.district;

import gov.nysenate.sage.client.view.district.MappedDistrictOverlapView;
import gov.nysenate.sage.client.view.map.PolygonMapView;
import gov.nysenate.sage.client.view.street.StreetRangeView;
import gov.nysenate.sage.model.address.DistrictedStreetRange;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictMatchLevel;
import gov.nysenate.sage.model.district.DistrictOverlap;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.DistrictResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MappedMultiDistrictResponse extends MappedDistrictResponse
{
    protected Map<String, List<MappedDistrictOverlapView>> overlaps = new HashMap<>();
    protected PolygonMapView referenceMap;
    protected PolygonMapView streetLine;
    protected BigDecimal totalReferenceArea;
    protected String areaUnit;
    protected String intersectType;
    protected List<StreetRangeView> streets = new ArrayList<>();

    public MappedMultiDistrictResponse(DistrictResult districtResult, DistrictType intersectType) {
        super(districtResult);
        DistrictInfo districtInfo = districtResult.getDistrictInfo();
        DistrictMatchLevel districtMatchLevel = districtResult.getDistrictMatchLevel();
        this.intersectType = (intersectType == null ? DistrictType.SENATE : intersectType).toString().toLowerCase();

        if (districtInfo != null) {
            if (!districtInfo.getDistrictOverlaps().isEmpty()) {
                this.referenceMap = new PolygonMapView(districtInfo.getReferenceMap());

                Map<DistrictType, DistrictOverlap> overlapsByDistrictType = districtInfo.getDistrictOverlaps();
                for (DistrictType districtType : overlapsByDistrictType.keySet()) {
                    DistrictOverlap overlap = overlapsByDistrictType.get(districtType);

                    /** Get the total reference area since they should all be the same for each overlap */
                    if (this.totalReferenceArea == null) {
                        this.totalReferenceArea = overlap.getTotalArea();
                        this.areaUnit = overlap.getAreaUnit().name();
                    }

                    if (overlap != null) {
                        List<MappedDistrictOverlapView> mappedOverlaps = new ArrayList<>();
                        for (String district : overlap.getOverlapDistrictCodes()) {
                            mappedOverlaps.add(new MappedDistrictOverlapView(overlap, district, districtMatchLevel));
                        }
                        overlaps.put(districtType.name().toLowerCase(), mappedOverlaps);
                    }
                }
            }
            /** Handle street line matches */
            this.streetLine = new PolygonMapView(districtInfo.getStreetLineReference());
            if (districtInfo.getStreetRanges() != null && !districtInfo.getStreetRanges().isEmpty()) {
                for (DistrictedStreetRange dsr : districtInfo.getStreetRanges()) {
                    this.streets.add(new StreetRangeView(dsr));
                }
            }
        }
    }

    public Map<String, List<MappedDistrictOverlapView>> getOverlaps() {
        return overlaps;
    }

    public PolygonMapView getReferenceMap() {
        return referenceMap;
    }

    public BigDecimal getTotalReferenceArea() {
        return totalReferenceArea;
    }

    public String getAreaUnit() {
        return areaUnit;
    }

    public PolygonMapView getStreetLine() {
        return streetLine;
    }

    public List<StreetRangeView> getStreets() {
        return streets;
    }

    public String getIntersectType() {
        return intersectType;
    }
}
