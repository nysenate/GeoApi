package gov.nysenate.sage.model.geo;

/**
 * The Geocode class represents the data obtained by an address geocoding
 * service. This includes the lat/lng pair represented by a Point and various
 * metrics describing the accuracy of the geocoding.
 */
public class Geocode
{
    /** Contains the lat lon pair returned by the geocoder */
    protected Point latlon;

    /** Geocoding quality metric */
    protected GeocodeQuality quality;

    /** Specify the geocoder that produced the geocode */
    protected String method;

    /** Unconverted geocoding quality metric returned by the geocoder */
    protected int rawQuality;

    public Geocode()
    {
        this(null, GeocodeQuality.NOMATCH, "");
    }

    public Geocode(Point latlon)
    {
        this(latlon, GeocodeQuality.UNKNOWN, "");
    }

    public Geocode(Point latlon, GeocodeQuality quality)
    {
        this(latlon, quality, "");
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
        return (this.latlon != null) ? this.latlon.getLat() : 0;
    }

    public void setLat(Double lat)
    {
        this.latlon.setLat(lat);
    }

    public void setLon(Double lon)
    {
        this.latlon.setLon(lon);
    }

    public Double getLon()
    {
        return (this.latlon != null) ? this.latlon.getLon() : 0;
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

    public void setRawQuality(int rawQuality)
    {
        this.rawQuality = rawQuality;
    }

    public int getRawQuality()
    {
        return this.rawQuality;
    }
}
