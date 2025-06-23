package gov.nysenate.sage.model.address;

import gov.nysenate.sage.model.geo.Geocode;

import java.io.Serializable;

/**
 * GeocodedAddress represents an address that contains geo coordinate information.
 */
// TODO: better typing for PO boxes?
public class GeocodedAddress implements Serializable {
    private Address address;
    private final Geocode geocode;

    public GeocodedAddress(Address address) {
        this(address, null);
    }

    public GeocodedAddress(Geocode geocode) {
        this(null, geocode);
    }

    public GeocodedAddress(Address address, Geocode geocode) {
        this.address = address;
        this.geocode = geocode;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Geocode getGeocode() {
        return geocode;
    }

    public boolean isValidAddress() {
        return address != null && address.isValid();
    }

    public boolean isValidGeocode() {
        return geocode != null && geocode.isValidGeocode();
    }
}
