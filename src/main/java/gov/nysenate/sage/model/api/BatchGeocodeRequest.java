package gov.nysenate.sage.model.api;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.geo.Point;

import java.util.ArrayList;
import java.util.List;

public class BatchGeocodeRequest extends GeocodeRequest
{
    protected List<Address> addresses = new ArrayList<>();
    protected List<Point> points = new ArrayList<>();

    public BatchGeocodeRequest() {}

    public BatchGeocodeRequest(GeocodeRequest gr) {
        super(gr.getApiRequest(), gr.getAddress(), gr.getProvider(), gr.isUseFallback(), gr.isUseCache());
    }

    public BatchGeocodeRequest(BatchDistrictRequest bdr) {
        super(bdr.getApiRequest(), null, bdr.getGeoProvider(), false, false);
        this.addresses = bdr.getAddresses();
        if (bdr.getGeoProvider() == null) {
            this.setUseFallback(true);
            this.setUseCache(true);
        }
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
