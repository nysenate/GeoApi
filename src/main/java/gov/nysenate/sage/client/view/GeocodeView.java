package gov.nysenate.sage.client.view;

import com.fasterxml.jackson.annotation.JsonRootName;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;

@JsonRootName("geocode")
public class GeocodeView
{
    protected double lat = 0.0;
    protected double lon = 0.0;
    protected String quality = GeocodeQuality.NOMATCH.name();

    public GeocodeView(Geocode geocode) {
        if (geocode != null) {
            this.lat = geocode.getLat();
            this.lon = geocode.getLon();
            this.quality = geocode.getQuality().name();
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
}
