package gov.nysenate.sage.model.api;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.provider.geocode.Geocoder;

public class SingleGeocodeRequest extends GeocodeRequest {
    private int id;
    private int addressId;

    /** Inputs */
    private Address address;
    private Point point;

    public SingleGeocodeRequest(ApiRequest apiRequest, Address address, String provider, boolean useFallback, boolean useCache) {
        super(apiRequest, Geocoder.getGeocoders(provider, useFallback, useCache), false, false, true);
        this.address = address;
    }

    public SingleGeocodeRequest(ApiRequest apiRequest, Address address, String provider, boolean useFallback, boolean useCache,
                                boolean doNotCache, boolean isUspsValidate) {
        super(apiRequest, Geocoder.getGeocoders(provider, useFallback, useCache), false, doNotCache, isUspsValidate);
        this.address = address;

    }

    public SingleGeocodeRequest(BatchGeocodeRequest batchRequest, Address address) {
        super(batchRequest.getApiRequest(), batchRequest.getGeocoders(), batchRequest.isReverse(),
                batchRequest.isDoNotCache(), batchRequest.isUspsValidate());
        this.address = address;
    }

    public SingleGeocodeRequest(BatchGeocodeRequest batchRequest, Point point) {
        super(batchRequest.getApiRequest(), batchRequest.getGeocoders(), batchRequest.isReverse(),
                batchRequest.isDoNotCache(), batchRequest.isUspsValidate());
        this.point = point;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAddressId() {
        return addressId;
    }

    public void setAddressId(int addressId) {
        this.addressId = addressId;
    }

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}