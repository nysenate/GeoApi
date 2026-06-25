package gov.nysenate.sage.client.response.map;

import gov.nysenate.sage.client.response.base.SourcedResponse;
import gov.nysenate.sage.client.view.map.DistrictMapView;
import gov.nysenate.sage.client.view.map.PolygonMapView;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.MapResult;

public class MapResponse extends SourcedResponse {
    protected DistrictMapView map;

    public MapResponse(MapResult mapResult, boolean showMaps) {
        super(mapResult);
        if (mapResult != null && mapResult.isSuccess()) {
            map = new DistrictMapView(mapResult.getDistrictMap(), showMaps);
        }
    }

    public String getDistrict() {
        return (map != null) ? map.getDistrict() : null;
    }

    public String getName() {
        return (map != null) ? map.getName() : null;
    }

    public DistrictType getType() {
        return (map != null) ? map.getType() : null;
    }

    public PolygonMapView getMap() {
        return (map != null) ? map.getMap() : null;
    }

    public Object getMember() {
        return (map != null) ? map.getMember() : null;
    }

    public String getLink() {
        return (map != null) ? map.getLink() : null;
    }
}
