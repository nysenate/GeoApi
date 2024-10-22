package gov.nysenate.sage.model.address;

import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;

/**
 * GeocodedStreetAddress represents a street address that contains geo coordinate information.
 */
public class GeocodedStreetAddress {
    protected StreetAddress streetAddress;
    protected Geocode geocode;

    public GeocodedStreetAddress(StreetAddress streetAddress, Geocode geocode) {
        this.streetAddress = streetAddress;
        this.geocode = geocode;
    }

    public StreetAddress getStreetAddress() {
        return streetAddress;
    }

    public Geocode getGeocode() {
        return geocode;
    }

    public GeocodedAddress toGeocodedAddress() {
        return new GeocodedAddress((streetAddress != null) ? streetAddress.toAddress() : null, geocode);
    }

    public boolean isGeocoded() {
        return (this.geocode != null && this.geocode.quality() != GeocodeQuality.NOMATCH);
    }
}
