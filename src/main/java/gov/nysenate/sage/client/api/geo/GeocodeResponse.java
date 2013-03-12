package gov.nysenate.sage.client.api.geo;

import gov.nysenate.sage.client.api.BaseResponse;
import gov.nysenate.sage.client.model.SageAddress;
import gov.nysenate.sage.client.model.SageGeocode;
import gov.nysenate.sage.model.result.GeocodeResult;

public class GeocodeResponse extends BaseResponse
{
    protected SageAddress address;
    protected SageGeocode geocode;
    protected boolean isGeocoded = false;

    public GeocodeResponse(GeocodeResult geocodeResult) {
        super(geocodeResult);
        if (geocodeResult != null && geocodeResult.isSuccess()) {
            this.address = new SageAddress(geocodeResult.getGeocodedAddress().getAddress());
            this.geocode = new SageGeocode(geocodeResult.getGeocodedAddress().getGeocode());
            this.isGeocoded = true;
        }
    }

    public SageAddress getAddress() {
        return address;
    }

    public SageGeocode getGeocode() {
        return geocode;
    }

    public boolean isGeocoded() {
        return isGeocoded;
    }
}
