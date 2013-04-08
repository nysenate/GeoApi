package gov.nysenate.sage.model.address;

import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;

/**
 * GeocodedStreetAddress represents a street address that contains geo coordinate information.
 */
public class GeocodedStreetAddress
{
    protected StreetAddress streetAddress;
    protected Geocode geocode;

    public GeocodedStreetAddress() {}

    public GeocodedStreetAddress(StreetAddress streetAddress)
    {
        this(streetAddress, null);
    }

    public GeocodedStreetAddress(StreetAddress streetAddress, Geocode geocode)
    {
        this.setStreetAddress(streetAddress);
        this.setGeocode(geocode);
    }

    public StreetAddress getStreetAddress()
    {
        return streetAddress;
    }

    public void setStreetAddress(StreetAddress streetAddress)
    {
        this.streetAddress = streetAddress;
    }

    public void setGeocode(Geocode geocode)
    {
        this.geocode = geocode;
    }

    public Geocode getGeocode()
    {
        return this.geocode;
    }

    public GeocodedAddress toGeocodedAddress()
    {
        return new GeocodedAddress((streetAddress != null) ? streetAddress.toAddress() : null, geocode);
    }

    public boolean isGeocoded()
    {
        return (this.geocode != null && this.geocode.getQuality() != GeocodeQuality.NOMATCH);
    }
}
