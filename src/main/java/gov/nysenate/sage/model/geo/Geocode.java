package gov.nysenate.sage.model.geo;

import gov.nysenate.sage.provider.geocode.Geocoder;

import java.math.BigDecimal;

/**
 * The Geocode class represents the data obtained by an address geocoding
 * service. This includes the lat/log pair represented by a Point and various
 * metrics describing the accuracy of the geocoding.
 */
public record Geocode(Point point, GeocodeQuality quality, Geocoder originalGeocoder, boolean isCached) {
    public BigDecimal lat() {
        return point.lat();
    }

    public BigDecimal lon() {
        return point.lon();
    }

    /** A valid geocode should have a quality code level of REGION or greater */
    public boolean isValidGeocode() {
        return quality() != null && quality().compareTo(GeocodeQuality.REGION) >= 0;
    }

    public Geocoder geocoder() {
        if (isCached) {
            return Geocoder.GEOCACHE;
        }
        return originalGeocoder;
    }
}
