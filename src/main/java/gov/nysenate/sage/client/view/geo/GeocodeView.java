package gov.nysenate.sage.client.view.geo;

import com.google.openlocationcode.OpenLocationCode;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;

/**
 * GeocodeView represents the structure of a geocode on the response end of the API.
 */
public class GeocodeView {
    protected String lat = null;
    protected String lon = null;
    protected String quality = GeocodeQuality.NOMATCH.name();
    protected String method = "";
    protected boolean cached = false;
    protected String openLocCode = "";

    public GeocodeView(Geocode geocode) {
        if (geocode != null) {
            this.lat = geocode.lat().toString();
            this.lon = geocode.lon().toString();
            this.quality = geocode.quality().name();
            this.method = geocode.originalGeocoder().name();
            this.cached = geocode.isCached();
            // TODO: change this
            this.openLocCode = OpenLocationCode.encode(geocode.lat().doubleValue(), geocode.lon().doubleValue());
        }
    }

    public String getLat() {
        return lat;
    }

    public String getLon() {
        return lon;
    }

    public String getQuality() {
        return quality;
    }

    public String getMethod() {
        return method;
    }

    public boolean isCached() {
        return cached;
    }

    public String getOpenLocCode() {
        return openLocCode;
    }
}
