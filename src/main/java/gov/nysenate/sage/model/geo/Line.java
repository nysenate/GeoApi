package gov.nysenate.sage.model.geo;

import java.util.List;

/**
 * Simple line representation
 */
public class Line {
    protected final List<Point> points;

    /**
     * Construct a line object with the provided list of points.
     * @param points list of points
     */
    public Line(List<Point> points) {
        this.points = points;
    }

    /**
     * Return the list of points that comprise this line.
     * @return list of points that represent the line
     */
    public List<Point> getPoints() {
        return points;
    }
}
