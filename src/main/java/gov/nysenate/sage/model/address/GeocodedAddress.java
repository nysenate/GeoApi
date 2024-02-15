package gov.nysenate.sage.model.address;

import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;

import java.io.Serializable;

/**
 * GeocodedAddress represents an address that contains geo coordinate information.
 */
public class GeocodedAddress implements Serializable, Cloneable
{
    protected Address address;
    protected Geocode geocode;

    public GeocodedAddress() {}

    public GeocodedAddress(Address address)
    {
        this(address, null);
    }

    public GeocodedAddress(Geocode geocode) {
        this(null, geocode);
    }

    public GeocodedAddress(Address address, Geocode geocode)
    {
        this.setAddress(address);
        this.setGeocode(geocode);
    }

    public Address getAddress()
    {
        return address;
    }

    public void setAddress(Address address)
    {
        this.address = address;
    }

    public void setGeocode(Geocode geocode)
    {
        this.geocode = geocode;
    }

    public Geocode getGeocode()
    {
        return this.geocode;
    }

    /** Basic check on the address */
    public boolean isValidAddress()
    {
        return isReverseGeocoded();
    }

    /** A valid geocode should have a quality code level of CITY or greater */
    public boolean isValidGeocode()
    {
        return (this.geocode != null && this.geocode.getQuality() != null
                && this.geocode.getQuality().compareTo(GeocodeQuality.CITY) >= 0);
    }

    public boolean isReverseGeocoded()
    {
        return (this.address != null && !this.address.isEmpty());
    }
}
