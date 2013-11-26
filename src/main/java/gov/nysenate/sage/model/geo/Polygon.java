package gov.nysenate.sage.model.geo;

import java.util.List;

/**
 * Simple polygon representation
 */
public class Polygon extends Line
{
    /**
     * Construct a Polygon object with the provided list of points.*
     * @param points list of points
    */
    public Polygon(List<Point> points) {
        super(points);
    }

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
        for (Point p : points) {
            s.append(p.toString());
            s.append("\n");
        }
        return s.toString();
    }
}
