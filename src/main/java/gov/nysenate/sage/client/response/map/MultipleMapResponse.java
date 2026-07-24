package gov.nysenate.sage.client.response.map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import gov.nysenate.sage.client.response.base.SourcedResponse;
import gov.nysenate.sage.client.view.map.DistrictMapView;
import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.result.MapListResult;

import java.util.*;

public class MultipleMapResponse extends SourcedResponse {
    private final List<DistrictMapView> districtMapViews = new ArrayList<>();

    public MultipleMapResponse(MapListResult mapResult) {
        super(mapResult);
        if (mapResult != null && mapResult.isSuccess()) {
            for (DistrictMap map : mapResult.getDistrictMaps().values()) {
                districtMapViews.add(new DistrictMapView(map));
            }
        }
    }

    @JsonIgnore
    public List<DistrictMapView> getMapViews() {
        return districtMapViews;
    }

    // Ensures maps show up in dropdowns in the proper order.
    public List<DistrictMapView> getDistricts() {
        return districtMapViews.stream().sorted(MultipleMapResponse::getComparator).toList();
    }

    private static int getComparator(DistrictMapView o1, DistrictMapView o2) {
        String baseName1 = String.valueOf(o1.getName()).split(" of ")[0];
        String baseName2 = String.valueOf(o2.getName()).split(" of ")[0];
        int i = Arrays.mismatch(baseName1.toCharArray(), baseName2.toCharArray());
        if (i < 0) {
            return 0;
        }
        // It's common for names to have a common form, e.g. District x, where x is the code.
        // These should be sorted by the code.
        baseName1 = baseName1.substring(i);
        baseName2 = baseName2.substring(i);
        try {
            return Long.compare(Long.parseLong(baseName1), Long.parseLong(baseName2));
        } catch (NumberFormatException ex) {
            return baseName1.compareTo(baseName2);
        }
    }
}
