package gov.nysenate.sage.dao.stats;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.stats.Deployment;
import gov.nysenate.sage.model.stats.DeploymentStats;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class DeploymentStatsDao extends BaseDao
{
    private static Logger logger = Logger.getLogger(DeploymentStatsDao.class);
    private String SCHEMA = "log";
    private String TABLE = "deployment";
    private static ResultSetHandler<List<Deployment>> listHandler = new BeanListHandler<>(Deployment.class);
    private QueryRunner run = getQueryRunner();

    /**
     * Get DeploymentStats from the database
     * @return DeploymentStats
     */
    public DeploymentStats getDeploymentStats()
    {
        String sql = "SELECT id, deployed, refId AS deploymentRef, deployTime, apiRequestsSince \n" +
                     "FROM " + SCHEMA + "." + TABLE + " \n" +
                     "ORDER BY deploytime ASC";
        try {
            List<Deployment> deployments = run.query(sql, listHandler);
            return new DeploymentStats(deployments);
        }
        catch (SQLException ex) {
            logger.error("Failed to get deployment stats", ex);
        }
        return null;
    }

    /**
     * Get deployment stats for a given time range.
     * @param since Timestamp for the beginning of the range.
     * @param until Timestamp for the end of the range.
     * @return DeploymentStats
     */
    public DeploymentStats getDeploymentStatsDuring(Timestamp since, Timestamp until)
    {
        String sql = "SELECT id, deployed, refId AS deploymentRef, deployTime, apiRequestsSince \n" +
                     "FROM " + SCHEMA + "." + TABLE + " \n" +
                     "WHERE deployTime >= ? AND deployTime <= ?";
        try {
            List<Deployment> deployments = run.query(sql, listHandler, since, until);
            return new DeploymentStats(deployments);
        }
        catch (SQLException ex) {
            logger.error("Failed to get deployment stats", ex);
        }
        return null;
    }
}
