package gov.nysenate.sage.client.response.geo;

import gov.nysenate.sage.client.response.base.BaseResponse;
import gov.nysenate.sage.client.view.address.AddressView;
import gov.nysenate.sage.client.view.geo.GeocodeView;
import gov.nysenate.sage.model.result.GeocodeResult;

public class RevGeocodeResponse extends BaseResponse
{
    protected AddressView address;
    protected GeocodeView geocode;
    protected boolean isRevGeocoded = false;

    public RevGeocodeResponse(GeocodeResult geocodeResult) {
        super(geocodeResult);
        if (geocodeResult != null && geocodeResult.isSuccess()) {
            this.address = new AddressView(geocodeResult.getGeocodedAddress().getAddress());
            this.geocode = new GeocodeView(geocodeResult.getGeocode());
            this.isRevGeocoded = true;
        }
    }

    public AddressView getAddress() {
        return address;
    }

    public GeocodeView getGeocode() {
        return geocode;
    }

    public boolean isRevGeocoded() {
        return isRevGeocoded;
    }
}
