package gov.nysenate.sage.model.api;

import gov.nysenate.sage.provider.geocode.Geocoder;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public abstract class GeocodeRequest {
    private final List<Geocoder> geocoders;
    private final Timestamp requestTime = new Timestamp(new Date().getTime());
    private boolean isUspsValidate;

    public GeocodeRequest(List<Geocoder> geocoders, boolean isUspsValidate) {
        this.geocoders = geocoders;
        this.isUspsValidate = isUspsValidate;
    }

    public List<Geocoder> getGeocoders() {
        return geocoders;
    }

    public Timestamp getRequestTime() {
        return requestTime;
    }

    public boolean isUspsValidate() {
        return isUspsValidate;
    }

    public void setUspsValidate(boolean uspsValidate) {
        isUspsValidate = uspsValidate;
    }
}
