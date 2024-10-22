package gov.nysenate.sage.model.geo;

import gov.nysenate.sage.provider.geocode.Geocoder;
import org.jsoup.internal.FieldsAreNonnullByDefault;

/**
 * The Geocode class represents the data obtained by an address geocoding
 * service. This includes the lat/log pair represented by a Point and various
 * metrics describing the accuracy of the geocoding.
 */
@FieldsAreNonnullByDefault
public record Geocode(Point point, GeocodeQuality quality, Geocoder originalGeocoder, boolean isCached) {
    public Geocode(Point point, GeocodeQuality quality, String originalGeocoder) {
        this(point, quality, originalGeocoder, false);
    }

    public Geocode(Point point, GeocodeQuality quality, String originalGeocoder, boolean isCached) {
        this(point, quality, Geocoder.valueOf(originalGeocoder.toUpperCase().trim()), isCached);
    }

    public double lat() {
        return point.lat();
    }

    public double lon() {
        return point.lon();
    }
}
