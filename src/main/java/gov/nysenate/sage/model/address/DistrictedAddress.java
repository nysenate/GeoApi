package gov.nysenate.sage.model.address;

import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.geo.Geocode;

import java.io.Serializable;

/**
 * Represents an address with district information.
 */
public class DistrictedAddress implements Serializable {
    private Address address;
    private Geocode geocode;
    private DistrictInfo districtInfo = new DistrictInfo();

    public DistrictedAddress() {}

    public DistrictedAddress(GeocodedAddress geocodedAddress, DistrictInfo districtInfo) {
        this.address = geocodedAddress.getAddress();
        this.geocode = geocodedAddress.getGeocode();
        this.districtInfo = districtInfo;
    }

    /** Convenience method to access the underlying Address object */
    public Address getAddress() {
        return address;
    }

    /** Convenience method to set the underlying Address object */
    public void setAddress(Address address) {
        this.address = address;
    }

    /** Convenience method to set the underlying Geocode object */
    public void setGeocode(Geocode geocode) {
        this.geocode = geocode;
    }

    /** Convenience method to get the underlying Geocode object */
    public Geocode getGeocode() {
        return geocode;
    }

    public DistrictInfo getDistrictInfo() {
        return districtInfo;
    }

    public void setDistrictInfo(DistrictInfo districtInfo) {
        this.districtInfo = districtInfo;
    }
}
