package gov.nysenate.sage.client.response.geo;

import gov.nysenate.sage.model.result.GeocodeResult;

public class RevGeocodeResponse extends BaseGeocodeResponse {
    private final boolean isRevGeocoded;

    public RevGeocodeResponse(GeocodeResult geocodeResult) {
        super(geocodeResult);
        this.isRevGeocoded = geocodeResult != null && geocodeResult.isSuccess();
    }

    public boolean isRevGeocoded() {
        return isRevGeocoded;
    }
}
