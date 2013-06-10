package gov.nysenate.sage.client.view;

import com.fasterxml.jackson.annotation.JsonRootName;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;

/**
 * GeocodeView represents the structure of a geocode on the response end of the API.
 */
@JsonRootName("geocode")
public class GeocodeView
{
    protected double lat = 0.0;
    protected double lon = 0.0;
    protected String quality = GeocodeQuality.NOMATCH.name();
    protected String method = "";

    public GeocodeView(Geocode geocode)
    {
        if (geocode != null) {
            this.lat = geocode.getLat();
            this.lon = geocode.getLon();
            this.quality = geocode.getQuality().name();
            this.method = geocode.getMethod();
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
}
