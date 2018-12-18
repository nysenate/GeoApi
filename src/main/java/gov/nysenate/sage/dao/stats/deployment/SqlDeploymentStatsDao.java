package gov.nysenate.sage.dao.stats.deployment;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.stats.Deployment;
import gov.nysenate.sage.model.stats.DeploymentStats;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class SqlDeploymentStatsDao implements DeploymentStatsDao
{
    private static Logger logger = LoggerFactory.getLogger(SqlDeploymentStatsDao.class);
    private String SCHEMA = "log";
    private String TABLE = "deployment";
    private static ResultSetHandler<List<Deployment>> listHandler = new BeanListHandler<>(Deployment.class);
    private QueryRunner run;
    private BaseDao baseDao;

    @Autowired
    public SqlDeploymentStatsDao(BaseDao baseDao) {
        this.baseDao = baseDao;
        run = this.baseDao.getQueryRunner();
    }

    /**
     * Get DeploymentStats from the database
     * @return DeploymentStats
     */
    public DeploymentStats getDeploymentStats()
    {
        try {
            List<Deployment> deployments = baseDao.geoApiNamedJbdcTemaplate.query(
                    DeploymentStatsQuery.SELECT_DEPLOY_STATS.getSql(baseDao.getLogSchema()), new DeploymentStatsMapper());
            return new DeploymentStats(deployments);
        }
        catch (Exception ex) {
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
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("since", since);
            params.addValue("until", until);

            List<Deployment> deployments = baseDao.geoApiNamedJbdcTemaplate.query(
                    DeploymentStatsQuery.SELECT_TIME_RANGE_STATS.getSql(
                            baseDao.getLogSchema()), params, new DeploymentStatsMapper());
            return new DeploymentStats(deployments);
        }
        catch (Exception ex) {
            logger.error("Failed to get deployment stats", ex);
        }
        return null;
    }

    private static class DeploymentStatsMapper implements RowMapper<Deployment> {
        public Deployment mapRow(ResultSet rs, int rowNum) throws SQLException {
            Deployment deployment = new Deployment();
            deployment.setId(rs.getInt("id"));
            deployment.setDeployed(rs.getBoolean("deployed"));
            deployment.setDeploymentRef(rs.getInt("deploymentRef"));
            deployment.setDeployTime(rs.getTimestamp("deployTime"));
            deployment.setApiRequestsSince(rs.getInt("apiRequestsSince"));
            return deployment;
        }
    }
}
