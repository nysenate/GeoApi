package gov.nysenate.sage.client.response.map;

import gov.nysenate.sage.client.response.base.BaseResponse;
import gov.nysenate.sage.client.view.map.DistrictMapView;
import gov.nysenate.sage.client.view.map.MapView;
import gov.nysenate.sage.model.result.MapResult;

public class MapResponse extends BaseResponse
{
    protected DistrictMapView map;

    public MapResponse(MapResult mapResult) {
        super(mapResult);
        if (mapResult != null && mapResult.isSuccess()) {
            map = new DistrictMapView(mapResult.getDistrictMap());
        }
    }

    public String getDistrict() {
        return (map != null) ? map.getDistrict() : null;
    }

    public String getName() {
        return (map != null) ? map.getName() : null;
    }

    public String getType() {
        return (map != null) ? map.getType() : null;
    }

    public MapView getMap() {
        return (map != null) ? map.getMap() : null;
    }

    public Object getMember() {
        return (map != null) ? map.getMember() : null;
    }
}
