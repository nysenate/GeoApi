package gov.nysenate.sage.model.address;

import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictMatchLevel;
import gov.nysenate.sage.model.geo.Geocode;

import java.io.Serializable;

/**
 * Represents an address with district information.
 */
public class DistrictedAddress implements Serializable {
    private Address address;
    private Geocode geocode;
    private DistrictInfo districtInfo = new DistrictInfo();
    private DistrictMatchLevel districtMatchLevel = DistrictMatchLevel.NOMATCH;

    public DistrictedAddress() {}

    public DistrictedAddress(GeocodedAddress geocodedAddress, DistrictInfo districtInfo) {
        this(geocodedAddress, districtInfo, DistrictMatchLevel.NOMATCH);
    }

    public DistrictedAddress(GeocodedAddress geocodedAddress, DistrictInfo districtInfo, DistrictMatchLevel districtMatchLevel) {
        this(geocodedAddress.getAddress(), geocodedAddress.getGeocode(), districtInfo, districtMatchLevel);
    }

    public DistrictedAddress(Address address, Geocode geocode, DistrictInfo districtInfo, DistrictMatchLevel districtMatchLevel) {
        this.address = address;
        this.geocode = geocode;
        this.districtInfo = districtInfo;
        this.districtMatchLevel = districtMatchLevel;
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

    public DistrictMatchLevel getDistrictMatchLevel() {
        return districtMatchLevel;
    }

    public void setDistrictMatchLevel(DistrictMatchLevel districtMatchLevel) {
        this.districtMatchLevel = districtMatchLevel;
    }
}
