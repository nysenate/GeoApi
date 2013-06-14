package gov.nysenate.sage.client.response.map;

import gov.nysenate.sage.client.response.base.BaseResponse;
import gov.nysenate.sage.client.view.map.DistrictMapView;
import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.result.MapResult;

import java.util.ArrayList;
import java.util.List;

public class MultipleMetadataResponse extends BaseResponse
{
    protected List<DistrictMapView> districts = new ArrayList<>();

    public MultipleMetadataResponse(MapResult mapResult)
    {
        super(mapResult);
        if (mapResult != null) {
            if (mapResult.isSuccess()) {
                for (DistrictMap districtMap : mapResult.getDistrictMaps()) {
                    districts.add(new DistrictMapView(districtMap, false));
                }
            }
        }
    }

    public List<DistrictMapView> getDistricts() {
        return districts;
    }
}
