package gov.nysenate.sage.model.address;

import java.util.List;

public class GeocodedPostOfficeBox extends GeocodedAddress {
    private final List<GeocodedAddress> postOffices;

    public GeocodedPostOfficeBox(PostOfficeBox poBox, List<GeocodedAddress> postOffices) {
        super(poBox, postOffices.size() == 1 ? postOffices.get(0).getGeocode() : null);
        this.postOffices = postOffices;
    }

    public List<GeocodedAddress> getPostOffices() {
        return postOffices;
    }
}
