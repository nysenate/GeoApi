package gov.nysenate.sage.client.view.geo;

import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;

/**
 * GeocodeView represents the structure of a geocode on the response end of the API.
 */
public class GeocodeView
{
    protected double lat = 0.0;
    protected double lon = 0.0;
    protected String quality = GeocodeQuality.NOMATCH.name();
    protected String method = "";
    protected boolean cached = false;
    protected String openLocCode = "";

    public GeocodeView(Geocode geocode)
    {
        if (geocode != null) {
            this.lat = geocode.getLat();
            this.lon = geocode.getLon();
            this.quality = geocode.getQuality().name();
            this.method = geocode.getMethod();
            this.cached = geocode.isCached();
            this.openLocCode = geocode.getOpenLocCode();
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

    public String getOpenLocCode() { return openLocCode; }
}
