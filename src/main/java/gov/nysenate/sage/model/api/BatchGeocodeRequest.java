package gov.nysenate.sage.model.api;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.provider.geocode.Geocoder;

import java.util.ArrayList;
import java.util.List;

public class BatchGeocodeRequest extends GeocodeRequest {
    protected List<Address> addresses = new ArrayList<>();
    protected List<Point> points = new ArrayList<>();

    public BatchGeocodeRequest(List<Address> addresses) {
        super(null, null, false, false, true);
        this.addresses = addresses;
    }

    public BatchGeocodeRequest(SingleGeocodeRequest gr) {
        super(gr.getApiRequest(), gr.getGeocoders(), gr.isReverse(), gr.isDoNotCache(), gr.isUspsValidate());
    }

    public BatchGeocodeRequest(BatchDistrictRequest bdr) {
        super(bdr.getApiRequest(), Geocoder.getGeocoders(bdr.getGeoProvider(), true, true),
                false, false, bdr.isUspsValidate());
        this.addresses = bdr.getAddresses();
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    public List<Point> getPoints() {
        return points;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
    }
}
