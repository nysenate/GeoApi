package gov.nysenate.sage.model.api;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.service.district.DistrictServiceProvider;

import java.util.ArrayList;
import java.util.List;

public class BatchDistrictRequest extends DistrictRequest
{
    /** Original user input */
    private List<Address> addresses = new ArrayList<>();
    private List<Point> points = new ArrayList<>();

    /** Geocoded input */
    private List<GeocodedAddress> geocodedAddresses = new ArrayList<>();

    public BatchDistrictRequest() {}

    public BatchDistrictRequest(DistrictRequest dr)
    {
        super(dr.getApiRequest(), dr.getAddress(), dr.getProvider(), dr.getGeoProvider(), dr.isShowMembers(), dr.isShowMaps(),
              dr.isUspsValidate(), dr.isSkipGeocode(), dr.getDistrictStrategy());
    }

    public List<Point> getPoints() {
        return points;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
    }

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
