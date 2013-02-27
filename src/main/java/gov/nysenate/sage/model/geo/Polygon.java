package gov.nysenate.sage.model.geo;

import gov.nysenate.sage.model.geo.Point;

import java.lang.StringBuilder;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Ken Zalewski
 */

public class Polygon
{
    private List<Point> m_points;


    /**
     * Construct an empty polygon object.
     */
    public Polygon()
    {
        m_points = new ArrayList<Point>();
    } // Polygon()


    /**
     * Construct a polygon object with the provided list of points.
     *
     * @param points list of points
     */
    public Polygon(List<Point> points)
    {
        m_points = points;
    } // Polygon()


    /**
     * Return the list of points that comprise this polygon.
     *
     * @return list of points that represent the polygon
     */
    public List<Point> getPoints()
    {
        return m_points;
    } // getPoints()


    /**
     * Sets the polygon to the provided list of points.
     *
     * @param points list of points
     */
    public void setPoints(List<Point> points)
    {
        m_points = points;
    } // setPoints()


    /**
     * Append a Point to the end of the polygon.
     *
     * @param point the point to append
     */
    public void appendPoint(Point point)
    {
        m_points.add(point);
    } // appendPoint()


    /**
     * Clear out the points for this polygon.
     */
    public void clearPoints()
    {
        m_points.clear();
    } // clearPoints()


    /**
     * Get the number of points that comprise this polygon.
     *
     * @return number of points in this polygon
     */
    public int size()
    {
        return m_points.size();
    } // size()


    /**
     * Get the string representation of this polygon.  The string representation
     * is of the form: <point>NEWLINE<point>NEWLINE.....
     * The string representation of a Point is described elsewhere.
     *
     * @return string representation of this polygon
     */
    public String toString()
    {
        StringBuilder s = new StringBuilder();
        for (Point p : m_points) {
            s.append(p.toString());
            s.append("\n");
        }
        return s.toString();
    } // toString()


    /**
     * Get the JSON representation of this polygon.  The JSON representation
     * is of the form: [<point>,<point>,.....]
     * The JSON representation of a Point is described elsewhere.
     *
     * @return a string containing the JSON representation of this polygon
     */
    public String toJson()
    {
        StringBuilder s = new StringBuilder("[");
        boolean gotFirst = false;
        for (Point p : m_points) {
            if (gotFirst) {
                s.append(",");
            }
            else {
                gotFirst = true;
            }
            s.append(p.toJson());
        }
        s.append("]");
        return s.toString();
    } // toJson()
} // Polygon
