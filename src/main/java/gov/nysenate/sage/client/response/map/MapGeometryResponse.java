package gov.nysenate.sage.client.response.map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRawValue;
import gov.nysenate.sage.client.response.base.SourcedResponse;
import gov.nysenate.sage.client.view.map.DistrictMapView;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.MapResult;

public class MapGeometryResponse extends SourcedResponse {
    private DistrictMapView map;

    public MapGeometryResponse(MapResult mapResult) {
        super(mapResult);
        if (mapResult != null && mapResult.isSuccess()) {
            map = new DistrictMapView(mapResult.getDistrictMap());
        }
    }

    @JsonIgnore
    public DistrictMapView getMapView() {
        return map;
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

    @JsonRawValue
    public String getMap() {
        return (map != null) ? map.getMap() : null;
    }

    public Object getMember() {
        return (map != null) ? map.getMember() : null;
    }

    public String getLink() {
        return (map != null) ? map.getLink() : null;
    }
}
