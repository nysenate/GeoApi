package gov.nysenate.sage.model.address;

import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;

/**
 * GeocodedStreetAddress represents a street address that contains geo coordinate information.
 */
public record GeocodedStreetAddress(StreetAddress streetAddress, Geocode geocode) {
    public GeocodedAddress toGeocodedAddress() {
        return new GeocodedAddress((streetAddress != null) ? streetAddress.toAddress() : null, geocode);
    }

    public boolean isGeocoded() {
        return (this.geocode != null && this.geocode.quality() != GeocodeQuality.NOMATCH);
    }
}
