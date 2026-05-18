package gov.nysenate.sage.model.address;

import java.util.List;

public class GeocodedPostOfficeBox extends GeocodedAddress {
    private final List<GeocodedAddress> postOffices;

    public GeocodedPostOfficeBox(List<GeocodedAddress> postOffices) {
        super(postOffices.size() == 1 ? postOffices.getFirst().getGeocode() : null);
        this.postOffices = postOffices;
    }

    public List<GeocodedAddress> getPostOffices() {
        return postOffices;
    }
}
