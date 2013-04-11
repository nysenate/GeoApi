package gov.nysenate.sage.client.response;

import gov.nysenate.sage.client.view.DistrictMapView;
import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.result.MapResult;

import java.util.ArrayList;
import java.util.List;

public class MultipleMapResponse extends BaseResponse
{
    protected List<DistrictMapView> districts = new ArrayList<>();

    public MultipleMapResponse(MapResult mapResult)
    {
        super(mapResult);
        if (mapResult != null) {
            if (mapResult.isSuccess()) {
                for (DistrictMap districtMap : mapResult.getDistrictMaps()) {
                    districts.add(new DistrictMapView(districtMap));
                }
            }
        }
    }

    public List<DistrictMapView> getDistricts() {
        return districts;
    }
}
