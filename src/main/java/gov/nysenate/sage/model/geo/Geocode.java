package gov.nysenate.sage.model.geo;

/**
 * The Geocode class represents the data obtained by an address geocoding
 * service. This includes the lat/lng pair represented by a Point and various
 * metrics describing the accuracy of the geocoding.
 */
public class Geocode
{
    protected Point latlon;
    protected GeocodeQuality quality;
    protected String method;

    public Geocode()
    {
        this(null, GeocodeQuality.NOMATCH, "");
    }

    public Geocode(Point latlon, GeocodeQuality quality, String method)
    {
        this.latlon = latlon;
        this.quality = quality;
        this.method = method;
    }

    public Point getLatLon()
    {
        return latlon;
    }

    public void setLatlon(Point latlon)
    {
        this.latlon = latlon;
    }

    public void setLatlon(Double lat, Double lon)
    {
        this.latlon = new Point(lat,lon);
    }

    public Double getLat()
    {
        return (this.latlon != null) ? this.latlon.getLatitude() : 0;
    }

    public Double getLon()
    {
        return (this.latlon != null) ? this.latlon.getLongitude() : 0;
    }

    public GeocodeQuality getQuality()
    {
        return quality;
    }

    public void setQuality(GeocodeQuality quality)
    {
        this.quality = quality;
    }

    public String getMethod()
    {
        return method;
    }

    public void setMethod(String method)
    {
        this.method = method;
    }
}
