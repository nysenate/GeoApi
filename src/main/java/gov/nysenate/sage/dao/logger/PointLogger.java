package gov.nysenate.sage.dao.logger;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.base.ReturnIdHandler;
import gov.nysenate.sage.model.geo.Point;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.log4j.Logger;

import java.sql.SQLException;

public class PointLogger extends BaseDao
{
    private static Logger logger = Logger.getLogger(AddressLogger.class);
    private static String SCHEMA = "log";
    private static String TABLE = "point";
    private QueryRunner run = getQueryRunner();

    /**
     * Inserts a point into the points table. If a matching point already exists the id is returned instead of
     * inserting an identical entry.
     * @param point
     * @return int point id or -1 if not found
     */
    public int logPoint(Point point)
    {
        if (point != null) {
            int retrieveId = getPointId(point);
            if (retrieveId > 0) return retrieveId;

            String sql = "INSERT INTO " + SCHEMA + "." + TABLE + " (latlon)\n" +
                    "VALUES (ST_GeomFromText(?)) \n" +
                    "RETURNING id";
            try {
                return run.query(sql, new ReturnIdHandler(), "POINT (" + point.getLon() + " " + point.getLat() + ")");
            }
            catch (SQLException ex) {
                logger.error("Failed to log point!", ex);
            }
        }
        return -1;
    }

    /**
     * Attempt to retrieve the point id of the given Point
     * @param point
     * @return point id or -1 if not found
     */
    public int getPointId(Point point) {
        if (point != null) {
            String sql = "SELECT id FROM " + SCHEMA + "." + TABLE + "\n" +
                         "WHERE latlon = ST_GeomFromText(?)\n" +
                         "LIMIT 1";
            try {
                return run.query(sql, new ReturnIdHandler(), "POINT (" + point.getLon() + " " + point.getLat() + ")");
            }
            catch (SQLException ex) {
                logger.error("Failed to retrieve point id!", ex);
            }
        }
        return 0;
    }
}
