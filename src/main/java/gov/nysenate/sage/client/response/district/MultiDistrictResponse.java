package gov.nysenate.sage.client.response.district;

import gov.nysenate.sage.client.view.district.DistrictOverlapView;
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

public class MultiDistrictResponse extends DistrictResponse
{
    protected Map<String, List<DistrictOverlapView>> overlaps = new HashMap<>();
    protected BigDecimal totalReferenceArea;
    protected String areaUnit;
    protected List<StreetRangeView> streets = new ArrayList<>();

    public MultiDistrictResponse(DistrictResult districtResult)
    {
        super(districtResult);
        DistrictInfo districtInfo = districtResult.getDistrictInfo();
        DistrictMatchLevel districtMatchLevel = districtResult.getDistrictMatchLevel();

        if (districtInfo != null) {
            if (!districtInfo.getDistrictOverlaps().isEmpty()) {

                Map<DistrictType, DistrictOverlap> overlapsByDistrictType = districtInfo.getDistrictOverlaps();
                for (DistrictType districtType : overlapsByDistrictType.keySet()) {
                    DistrictOverlap overlap = overlapsByDistrictType.get(districtType);

                    /** Get the total reference area since they should all be the same for each overlap */
                    if (this.totalReferenceArea == null) {
                        this.totalReferenceArea = overlap.getTotalArea();
                        this.areaUnit = overlap.getAreaUnit().name();
                    }

                    if (overlap != null) {
                        List<DistrictOverlapView> overlapViews = new ArrayList<>();
                        for (String district : overlap.getOverlapDistrictCodes()) {
                            overlapViews.add(new DistrictOverlapView(overlap, district, districtMatchLevel));
                        }
                        overlaps.put(districtType.name().toLowerCase(), overlapViews);
                    }
                }
            }

            /** Handle street line matches */
            if (districtInfo.getStreetRanges() != null && !districtInfo.getStreetRanges().isEmpty()) {
                for (DistrictedStreetRange dsr : districtInfo.getStreetRanges()) {
                    this.streets.add(new StreetRangeView(dsr));
                }
            }
        }
    }

    public Map<String, List<DistrictOverlapView>> getOverlaps() {
        return overlaps;
    }

    public BigDecimal getTotalReferenceArea() {
        return totalReferenceArea;
    }

    public String getAreaUnit() {
        return areaUnit;
    }

    public List<StreetRangeView> getStreets() {
        return streets;
    }
}
