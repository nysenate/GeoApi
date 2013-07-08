package gov.nysenate.sage.model.geo;

import java.util.List;

/**
 * Simple line representation
 */
public class Line 
{
    protected List<Point> points;

    /**
     * Construct a line object with the provided list of points.*
     * @param points list of points
     */
    public Line(List<Point> points)
    {
        this.points = points;
    }

    /**
     * Return the list of points that comprise this line.*
     * @return list of points that represent the line
     */
    public List<Point> getPoints()
    {
        return points;
    }

    /**
     * Sets the line to the provided list of points.*
     * @param points list of points
     */
    public void setPoints(List<Point> points)
    {
        this.points = points;
    }

    /**
     * Append a Point to the end of the line.
     *
     * @param point the point to append
     */
    public void appendPoint(Point point)
    {
        points.add(point);
    }


    /**
     * Clear out the points for this line.
     */
    public void clearPoints()
    {
        points.clear();
    }

    /**
     * Get the number of points that comprise this line.*
     * @return number of points in this line
     */
    public int size()
    {
        return points.size();
    }
}
