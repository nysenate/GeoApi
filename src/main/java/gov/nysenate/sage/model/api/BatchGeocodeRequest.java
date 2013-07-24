package gov.nysenate.sage.model.api;

import gov.nysenate.sage.model.address.Address;

import java.util.List;

public class BatchGeocodeRequest extends GeocodeRequest
{
    protected List<Address> addresses;

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
}
