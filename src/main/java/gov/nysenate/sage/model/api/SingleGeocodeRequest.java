package gov.nysenate.sage.model.api;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.provider.geocode.Geocoder;

public class SingleGeocodeRequest extends GeocodeRequest {
    private int id;

    /** Inputs */
    private Address address;
    private Point point;

    public SingleGeocodeRequest(Address address, Geocoder baseProvider, boolean useFallback, boolean useCache) {
        super(Geocoder.getGeocoders(baseProvider, useFallback, useCache), false, false, true);
        this.address = address;
    }

    public SingleGeocodeRequest(Address address, Geocoder baseProvider, boolean useFallback, boolean useCache,
                                boolean doNotCache, boolean isUspsValidate) {
        super(Geocoder.getGeocoders(baseProvider, useFallback, useCache), false, doNotCache, isUspsValidate);
        this.address = address;

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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