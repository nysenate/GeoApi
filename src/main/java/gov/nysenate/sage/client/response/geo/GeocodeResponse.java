package gov.nysenate.sage.client.response.geo;

import gov.nysenate.sage.model.result.GeocodeResult;

public class GeocodeResponse extends BaseGeocodeResponse {
    private final boolean isGeocoded;

    public GeocodeResponse(GeocodeResult geocodeResult) {
        super(geocodeResult);
        this.isGeocoded = geocodeResult != null && geocodeResult.isSuccess();
    }

    public boolean isGeocoded() {
        return isGeocoded;
    }
}
