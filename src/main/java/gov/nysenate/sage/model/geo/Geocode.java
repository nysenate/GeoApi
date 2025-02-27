package gov.nysenate.sage.model.geo;

import gov.nysenate.sage.provider.geocode.Geocoder;

import java.math.BigDecimal;

/**
 * The Geocode class represents the data obtained by an address geocoding
 * service. This includes the lat/log pair represented by a Point and various
 * metrics describing the accuracy of the geocoding.
 */
public record Geocode(Point point, GeocodeQuality quality, Geocoder originalGeocoder, boolean isCached) {
    // TODO: may not need String at all
    public Geocode(Point point, GeocodeQuality quality, String originalGeocoder) {
        this(point, quality, originalGeocoder, false);
    }

    public Geocode(Point point, GeocodeQuality quality, String originalGeocoder, boolean isCached) {
        this(point, quality,
                originalGeocoder == null ? null : Geocoder.valueOf(originalGeocoder.toUpperCase().trim()),
                isCached);
    }

    public BigDecimal lat() {
        return point.lat();
    }

    public BigDecimal lon() {
        return point.lon();
    }

    /** A valid geocode should have a quality code level of CITY or greater */
    public boolean isValidGeocode() {
        return quality() != null && quality().compareTo(GeocodeQuality.CITY) >= 0;
    }
}
