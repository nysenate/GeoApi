package gov.nysenate.sage.dao.stats;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.stats.DeploymentStats;
import gov.nysenate.sage.model.stats.GeocodeStats;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Retrieves stats pertaining to geocoder usage.
 */
public class GeocodeStatsDao extends BaseDao
{
    private static Logger logger = Logger.getLogger(GeocodeStatsDao.class);
    private DeploymentStatsDao deploymentStatsDao = new DeploymentStatsDao();
    private QueryRunner run = getQueryRunner();

    /**
     * Retrieve geocode stats since first deployment
     * @return GeocodeStats
     */
    public GeocodeStats getLifetimeGeocodeStats()
    {
        Timestamp since = new Timestamp(0);
        Timestamp until = new Timestamp(new Date().getTime());
        return getGeocodeStatsDuring(since, until);
    }

    /**
     * Retrieve geocode stats since last deployment
     * @return GeocodeStats
     */
    public GeocodeStats getCurrentGeocodeStats()
    {
        DeploymentStats deploymentStats = deploymentStatsDao.getDeploymentStats();
        Timestamp since = deploymentStats.getLastDeploymentTime();
        Timestamp until = new Timestamp(new Date().getTime());
        return getGeocodeStatsDuring(since, until);
    }

    /**
     * Retrieve geocode stats within a specified time frame
     * @param since
     * @param until
     * @return GeocodeStats
     */
    public GeocodeStats getGeocodeStatsDuring(Timestamp since, Timestamp until)
    {
        String sql = "SELECT method, COUNT(*) AS usage \n" +
                     "FROM log.geocodeResults\n" +
                     "WHERE resultTime >= ? AND resultTime <= ? \n" +
                     "GROUP BY method";
        try {
            return run.query(sql, new ResultSetHandler<GeocodeStats>() {
                @Override
                public GeocodeStats handle(ResultSet rs) throws SQLException {
                    GeocodeStats gs = new GeocodeStats();
                    while (rs.next()) {
                        gs.addGeocoderUsage(rs.getString("method"), rs.getInt("usage"));
                    }
                    return gs;
                }
            }, since, until);
        }
        catch (SQLException ex) {
            logger.error("Failed to get geocode stats!", ex);
        }
        return null;
    }
}
