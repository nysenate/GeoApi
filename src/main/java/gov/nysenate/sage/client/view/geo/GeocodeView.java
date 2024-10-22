package gov.nysenate.sage.client.view.geo;

import com.google.openlocationcode.OpenLocationCode;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;

/**
 * GeocodeView represents the structure of a geocode on the response end of the API.
 */
public class GeocodeView {
    protected Double lat = null;
    protected Double lon = null;
    protected String quality = GeocodeQuality.NOMATCH.name();
    protected String method = "";
    protected boolean cached = false;
    protected String openLocCode = "";

    public GeocodeView(Geocode geocode) {
        if (geocode != null) {
            this.lat = geocode.lat();
            this.lon = geocode.lon();
            this.quality = geocode.quality().name();
            this.method = geocode.originalGeocoder().name();
            this.cached = geocode.isCached();
            this.openLocCode = OpenLocationCode.encode(lat, lon);
        }
    }

    public Double getLat() {
        return lat;
    }

    public Double getLon() {
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
