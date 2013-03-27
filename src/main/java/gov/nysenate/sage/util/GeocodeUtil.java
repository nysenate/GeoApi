package gov.nysenate.sage.util;

import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.Point;

public abstract class GeocodeUtil
{
    /**Returns the distance between two points in meters.
     * @param p1, p2    The Points to compare
     * @return  distance in feet, or -1.0 if either point is null     *
     * Adapted from http://www.movable-type.co.uk/scripts/latlong.html
     * */
    public static Double getDistanceInMeters(Point p1, Point p2)
    {
        if (p1 != null && p2 != null) {
            int r = 6371; // km
            double lat1 = p1.getLat();
            double lat2 = p2.getLat();
            double lon1 = p1.getLon();
            double lon2 = p2.getLon();

            double dLat = Math.toRadians(lat2-lat1);
            double dLon = Math.toRadians(lon2-lon1);
            lat1 = Math.toRadians(lat1);
            lat2 = Math.toRadians(lat2);

            double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                    Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
            double d = r * c;
            return d * 1000;  //Distance in meters
        }
        return -1.0;
    }

    /**
     * Returns the distance between two points in feet
     */
    public static Double getDistanceInFeet(Point p1, Point p2)
    {
        double meters = getDistanceInMeters(p1, p2);
        return (meters != -1.0) ? meters * 3.28084 : -1;
    }

    /**
     * Returns the distance between two geocodes in meters
     */
    public static Double getDistanceInMeters(Geocode g1, Geocode g2)
    {
        if ( g1 != null && g1.getLatLon() != null &&
                g2 != null && g2.getLatLon() != null) {
            return getDistanceInMeters(g1.getLatLon(), g2.getLatLon());
        }
        return -1.0;
    }

    /**
     * Returns the distance between two geocodes in feet
     */
    public static Double getDistanceInFeet(Geocode g1, Geocode g2)
    {
        double meters = getDistanceInMeters(g1, g2);
        return (meters != -1.0) ? meters * 3.28084 : -1;
    }
}