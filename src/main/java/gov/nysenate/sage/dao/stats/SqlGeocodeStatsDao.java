package gov.nysenate.sage.dao.stats;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.stats.GeocodeStats;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Retrieves stats pertaining to geocoder usage.
 */
@Repository
public class SqlGeocodeStatsDao
{
    private static Logger logger = LoggerFactory.getLogger(SqlGeocodeStatsDao.class);
    private SqlDeploymentStatsDao sqlDeploymentStatsDao;
    private BaseDao baseDao;
    private QueryRunner run;

    @Autowired
    public SqlGeocodeStatsDao(BaseDao baseDao, SqlDeploymentStatsDao sqlDeploymentStatsDao) {
        this.baseDao = baseDao;
        this.sqlDeploymentStatsDao = sqlDeploymentStatsDao;
        run = this.baseDao.getQueryRunner();
    }

    /**
     * Retrieve geocode stats within a specified time frame.
     * @return GeocodeStats
     */
    public GeocodeStats getGeocodeStats(Timestamp from, Timestamp to)
    {
        String totalCountsSql =
                "SELECT COUNT(*) AS totalGeocodes,\n" +
                "COUNT( DISTINCT resultTime ) AS totalRequests,\n" +
                "COUNT(NULLIF(cacheHit, false)) AS cacheHits\n" +
                "FROM log.geocodeResult\n" +
                "WHERE resultTime >= ? AND resultTime <= ?";

        String geocoderUsageSql =
                "SELECT replace(method, 'Dao', '') AS method, COUNT(DISTINCT resultTime) AS requests\n" +
                "FROM log.geocodeResult\n" +
                "WHERE cacheHit = false\n" +
                "AND resultTime >= ? AND resultTime <= ?\n" +
                "GROUP BY method\n" +
                "ORDER BY requests DESC";

        /** Handler for result set of totalCountsSql */
        class TotalCountsHandler implements ResultSetHandler<GeocodeStats> {
            @Override
            public GeocodeStats handle(ResultSet rs) throws SQLException {
                GeocodeStats gs = new GeocodeStats();
                if (rs.next()) {
                    gs.setTotalGeocodes(rs.getInt("totalGeocodes"));
                    gs.setTotalRequests(rs.getInt("totalRequests"));
                    gs.setTotalCacheHits(rs.getInt("cacheHits"));
                    return gs;
                }
                return null;
            }
        };

        /** Handler for result set of geocoderUsageSql */
        class GeocoderUsageHandler implements ResultSetHandler<GeocodeStats> {
            GeocodeStats gs;
            public GeocoderUsageHandler(GeocodeStats gs) {
                this.gs = gs;
            }

            @Override
            public GeocodeStats handle(ResultSet rs) throws SQLException {
                while (rs.next()) {
                    gs.addGeocoderUsage(rs.getString("method"), rs.getInt("requests"));
                }
                return gs;
            }
        }

        try {
            GeocodeStats gs = run.query(totalCountsSql, new TotalCountsHandler(), from, to);
            if (gs != null) {
                return run.query(geocoderUsageSql, new GeocoderUsageHandler(gs), from, to);
            }
        }
        catch (SQLException ex) {
            logger.error("Failed to get geocode stats!", ex);
        }
        return null;
    }
}