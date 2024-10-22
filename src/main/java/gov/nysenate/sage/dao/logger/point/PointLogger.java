package gov.nysenate.sage.dao.logger.point;

import gov.nysenate.sage.model.geo.Point;

public interface PointLogger {
    /**
     * Inserts a point into the points table. If a matching point already exists the ID is returned instead of
     * inserting an identical entry.
     * @return int point ID, or -1 if not found
     */
    int logPoint(Point point);

    /**
     * Attempt to retrieve the point ID of the given Point
     * @return point ID, or -1 if not found
     */
    int getPointId(Point point);
}
