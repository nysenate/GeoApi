package gov.nysenate.sage.client.response;

import gov.nysenate.sage.client.response.BaseResponse;
import gov.nysenate.sage.client.view.AddressView;
import gov.nysenate.sage.model.result.GeocodeResult;

public class RevGeocodeResponse extends BaseResponse
{
    protected AddressView address;
    protected boolean isRevGeocoded = false;

    public RevGeocodeResponse(GeocodeResult geocodeResult) {
        super(geocodeResult);
        if (geocodeResult != null && geocodeResult.isSuccess()) {
            this.address = new AddressView(geocodeResult.getGeocodedAddress().getAddress());
            this.isRevGeocoded = true;
        }
    }

    public AddressView getAddress() {
        return address;
    }

    public boolean isRevGeocoded() {
        return isRevGeocoded;
    }
}
