package gov.nysenate.sage.model.address;

import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;

import java.io.Serializable;

/**
 * GeocodedAddress represents an address that contains geo coordinate information.
 */
public class GeocodedAddress implements Serializable {
    protected Address address;
    protected Geocode geocode;

    public GeocodedAddress() {}

    public GeocodedAddress(Address address) {
        this(address, null);
    }

    public GeocodedAddress(Geocode geocode) {
        this(null, geocode);
    }

    public GeocodedAddress(Address address, Geocode geocode) {
        this.setAddress(address);
        this.setGeocode(geocode);
    }

    public static GeocodedAddress from(GeocodedAddress result, Address defaultAddress) {
        if (result == null) {
            return new GeocodedAddress(defaultAddress);
        }
        return result;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public void setGeocode(Geocode geocode) {
        this.geocode = geocode;
    }

    public Geocode getGeocode() {
        return this.geocode;
    }

    /** Basic check on the address */
    public boolean isValidAddress() {
        return isReverseGeocoded();
    }

    /** A valid geocode should have a quality code level of CITY or greater */
    public boolean isValidGeocode() {
        return geocode != null && geocode.quality() != null
                && geocode.quality().compareTo(GeocodeQuality.CITY) >= 0;
    }

    public boolean isReverseGeocoded() {
        return address != null && !address.isEmpty();
    }
}
