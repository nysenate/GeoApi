package gov.nysenate.sage.client.response.map;

import gov.nysenate.sage.client.response.base.SourcedResponse;
import gov.nysenate.sage.client.view.map.DistrictMapView;
import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.result.MapListResult;

import java.util.ArrayList;
import java.util.List;

public class MultipleMapResponse extends SourcedResponse {
    protected List<DistrictMapView> districts = new ArrayList<>();

    public MultipleMapResponse(MapListResult mapResult, boolean showMaps) {
        super(mapResult);
        if (mapResult != null && mapResult.isSuccess()) {
            for (DistrictMap districtMap : mapResult.getDistrictMaps()) {
                districts.add(new DistrictMapView(districtMap, showMaps));
            }
        }
    }

    public List<DistrictMapView> getDistricts() {
        return districts;
    }
}
