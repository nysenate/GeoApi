package gov.nysenate.sage.client.view.geo;

import com.google.openlocationcode.OpenLocationCode;
import gov.nysenate.sage.model.geo.Geocode;
import lombok.Getter;

/**
 * GeocodeView represents the structure of a geocode on the response end of the API.
 */
@Getter
public class GeocodeView {
    private final String lat;
    private final String lon;
    private final String quality;
    private final String method;
    private final boolean cached;
    private final String openLocCode;

    private GeocodeView(Geocode geocode) {
        this.lat = geocode.lat().toString();
        this.lon = geocode.lon().toString();
        this.quality = geocode.quality().name();
        this.method = geocode.originalGeocoder().name();
        this.cached = geocode.isCached();
        // Unfortunately, the only constructor uses doubles
        this.openLocCode = OpenLocationCode.encode(geocode.lat().doubleValue(), geocode.lon().doubleValue());
    }

    public static GeocodeView from(Geocode geocode) {
        if (geocode == null) {
            return null;
        }
        return new GeocodeView(geocode);
    }
}
