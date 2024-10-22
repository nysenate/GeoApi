package gov.nysenate.sage.client.response.map;

import gov.nysenate.sage.client.response.base.BaseResponse;
import gov.nysenate.sage.client.view.map.DistrictMapView;
import gov.nysenate.sage.model.result.MapResult;

public class MetadataResponse extends BaseResponse {
    protected DistrictMapView metadata;

    public MetadataResponse(MapResult mapResult) {
        super(mapResult);
        if (mapResult != null && mapResult.isSuccess()) {
            metadata = new DistrictMapView(mapResult.getDistrictMap(), false);
        }
    }

    public String getDistrict() {
        return (metadata != null) ? metadata.getDistrict() : null;
    }

    public String getName() {
        return (metadata!= null) ? metadata.getName() : null;
    }

    public String getType() {
        return (metadata != null) ? metadata.getType() : null;
    }

    public Object getMember() {
        return (metadata != null) ? metadata.getMember() : null;
    }
}
