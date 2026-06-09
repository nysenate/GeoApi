package gov.nysenate.sage.client.response.geo;

import gov.nysenate.sage.client.response.base.SourcedResponse;
import gov.nysenate.sage.client.view.address.AddressView;
import gov.nysenate.sage.client.view.geo.GeocodeView;
import gov.nysenate.sage.model.result.GeocodeResult;

public class BaseGeocodeResponse extends SourcedResponse {
    private AddressView address;
    private GeocodeView geocode;

    public BaseGeocodeResponse(GeocodeResult geocodeResult) {
        super(geocodeResult);
        if (geocodeResult != null && geocodeResult.isSuccess()) {
            this.address = new AddressView(geocodeResult.getGeocodedAddress().getAddress(), false);
            this.geocode = new GeocodeView(geocodeResult.getGeocode());
        }
    }

    public AddressView getAddress() {
        return address;
    }

    public GeocodeView getGeocode() {
        return geocode;
    }
}
