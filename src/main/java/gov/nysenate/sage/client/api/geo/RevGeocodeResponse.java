package gov.nysenate.sage.client.api.geo;

import gov.nysenate.sage.client.api.BaseResponse;
import gov.nysenate.sage.client.model.SageAddress;
import gov.nysenate.sage.model.result.GeocodeResult;

public class RevGeocodeResponse extends BaseResponse
{
    protected SageAddress address;
    protected boolean isRevGeocoded = false;

    public RevGeocodeResponse(GeocodeResult geocodeResult) {
        super(geocodeResult);
        if (geocodeResult != null && geocodeResult.isSuccess()) {
            this.address = new SageAddress(geocodeResult.getGeocodedAddress().getAddress());
            this.isRevGeocoded = true;
        }
    }

    public SageAddress getAddress() {
        return address;
    }

    public boolean isRevGeocoded() {
        return isRevGeocoded;
    }
}
