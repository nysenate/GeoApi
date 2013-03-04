package gov.nysenate.sage.model.geo;

/**
 * Simple point implementation
 * @author Ken Zalewski
 */

public class Point
{
    private double lat;
    private double lon;

    /**
     * Construct a Point using the provided latitude and longitude.
     *
     * @param lat the latitude as a Double number
     * @param lon the longitude as a Double number
     */
    public Point(double lat, double lon)
    {
        this.lat = lat;
        this.lon = lon;
    }

    /**
     * Retrieve the latitude associated with this Point.
     *
     * @return the latitude as a double-precision number
     */
    public double getLat()
    {
        return lat;
    }

    /**
     * Retrieve the longitude associated with this Point.
     *
     * @return the longitude as a double-precision number
     */
    public double getLon()
    {
        return lon;
    }

    /**
     * Get the string representation of this Point.  The string representation
     * is of the form <latitude>,<longitude>
     *
     * @return the string representation of this Point
     */
    public String toString()
    {
        return getLat()+","+ getLon();
    }

    /**
     * Get the JSON representation of this Point.  The JSON representation
     * is of the form [<latitude>,<longitude>]
     *
     * @return a string containing the JSON representation of this Point
     */
    public String toJson()
    {
        return "["+this.toString()+"]";
    }
}