package gov.nysenate.sage.client.response;

import gov.nysenate.sage.client.response.BaseResponse;
import gov.nysenate.sage.client.view.AddressView;
import gov.nysenate.sage.client.view.GeocodeView;
import gov.nysenate.sage.model.result.GeocodeResult;

public class GeocodeResponse extends BaseResponse
{
    protected AddressView address;
    protected GeocodeView geocode;
    protected boolean isGeocoded = false;

    public GeocodeResponse(GeocodeResult geocodeResult) {
        super(geocodeResult);
        if (geocodeResult != null && geocodeResult.isSuccess()) {
            this.address = new AddressView(geocodeResult.getGeocodedAddress().getAddress());
            this.geocode = new GeocodeView(geocodeResult.getGeocodedAddress().getGeocode());
            this.isGeocoded = true;
        }
    }

    public AddressView getAddress() {
        return address;
    }

    public GeocodeView getGeocode() {
        return geocode;
    }

    public boolean isGeocoded() {
        return isGeocoded;
    }
}