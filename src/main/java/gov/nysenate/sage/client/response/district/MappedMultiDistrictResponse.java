package gov.nysenate.sage.client.response.district;

import gov.nysenate.sage.client.view.district.MappedDistrictOverlapView;
import gov.nysenate.sage.client.view.district.MappedDistrictsView;
import gov.nysenate.sage.client.view.map.MapView;
import gov.nysenate.sage.model.district.DistrictInfo;
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
    protected BigDecimal totalReferenceArea;
    protected String areaUnit;

    public MappedMultiDistrictResponse(DistrictResult districtResult) {
        super(districtResult);
        DistrictInfo districtInfo = districtResult.getDistrictInfo();
        if (districtInfo != null && !districtInfo.getDistrictOverlaps().isEmpty()) {
            this.isMultiMatch = true;
            this.referenceMap = new MapView(districtInfo.getReferenceMap());

            Map<DistrictType, DistrictOverlap> overlapsByDistrict = districtInfo.getDistrictOverlaps();
            for (DistrictType districtType : overlapsByDistrict.keySet()) {
                DistrictOverlap overlap = overlapsByDistrict.get(districtType);

                /** Get the total reference area since they should all be the same for each overlap */
                if (this.totalReferenceArea == null) {
                    this.totalReferenceArea = overlap.getTotalArea();
                    this.areaUnit = overlap.getAreaUnit().name();
                }

                if (overlap != null) {
                    List<MappedDistrictOverlapView> mappedOverlaps = new ArrayList<>();
                    for (String district : overlap.getOverlapDistrictCodes()) {
                        mappedOverlaps.add(new MappedDistrictOverlapView(overlap, district));
                    }
                    overlaps.put(districtType.name().toLowerCase(), mappedOverlaps);
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
}
