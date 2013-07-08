package gov.nysenate.sage.client.response.district;

import gov.nysenate.sage.client.view.district.MappedDistrictOverlapView;
import gov.nysenate.sage.client.view.district.MappedDistrictsView;
import gov.nysenate.sage.client.view.map.MapView;
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
    protected String referenceDistrictType;
    protected MapView referenceMap;
    protected MapView streetLine;
    protected BigDecimal totalReferenceArea;
    protected String areaUnit;
    protected List<StreetRangeView> streets = new ArrayList<>();

    public MappedMultiDistrictResponse(DistrictResult districtResult) {
        super(districtResult);
        DistrictInfo districtInfo = districtResult.getDistrictInfo();
        DistrictMatchLevel districtMatchLevel = districtResult.getDistrictMatchLevel();

        if (districtInfo != null) {
            if (!districtInfo.getDistrictOverlaps().isEmpty()) {
                this.isMultiMatch = true;
                this.referenceMap = new MapView(districtInfo.getReferenceMap());

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
            this.streetLine = new MapView(districtInfo.getStreetLineReference());
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

    public String getReferenceDistrictType() {
        return referenceDistrictType;
    }

    public MapView getReferenceMap() {
        return referenceMap;
    }

    public BigDecimal getTotalReferenceArea() {
        return totalReferenceArea;
    }

    public String getAreaUnit() {
        return areaUnit;
    }

    public MapView getStreetLine() {
        return streetLine;
    }

    public List<StreetRangeView> getStreets() {
        return streets;
    }
}
