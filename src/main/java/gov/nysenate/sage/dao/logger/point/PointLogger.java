package gov.nysenate.sage.dao.logger.point;

import gov.nysenate.sage.model.geo.Point;

public interface PointLogger {
    /**
     * Inserts a point into the points table. If a matching point already exists the id is returned instead of
     * inserting an identical entry.
     * @param point
     * @return int point id or -1 if not found
     */
    public int logPoint(Point point);

    /**
     * Attempt to retrieve the point id of the given Point
     * @param point
     * @return point id or -1 if not found
     */
    public int getPointId(Point point);
}
