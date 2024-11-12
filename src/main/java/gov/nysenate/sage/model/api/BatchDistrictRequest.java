package gov.nysenate.sage.model.api;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.geo.Point;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class BatchDistrictRequest extends DistrictRequest {
    /** Original user input */
    private List<Address> addresses = new ArrayList<>();
    private List<Point> points = new ArrayList<>();

    /** Geocoded input */
    private List<GeocodedAddress> geocodedAddresses = new ArrayList<>();

    public BatchDistrictRequest() {}


    public BatchDistrictRequest(DistrictRequest dr) {
        this.provider = dr.getProvider();
        this.geoProvider = dr.getGeoProvider();
        this.uspsValidate = dr.isUspsValidate();
        this.skipGeocode = dr.isSkipGeocode();
        setDistrictStrategy(dr.getDistrictStrategy());
    }

    @Nonnull
    public List<Point> getPoints() {
        return points;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
    }

    @Nonnull
    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    public List<GeocodedAddress> getGeocodedAddresses() {
        return geocodedAddresses;
    }

    public void setGeocodedAddresses(List<GeocodedAddress> geocodedAddresses) {
        this.geocodedAddresses = geocodedAddresses;
    }
}
