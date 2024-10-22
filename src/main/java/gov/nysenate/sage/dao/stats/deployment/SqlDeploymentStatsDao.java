package gov.nysenate.sage.dao.stats.deployment;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.stats.Deployment;
import gov.nysenate.sage.model.stats.DeploymentStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class SqlDeploymentStatsDao implements DeploymentStatsDao
{
    private static Logger logger = LoggerFactory.getLogger(SqlDeploymentStatsDao.class);
    private BaseDao baseDao;

    @Autowired
    public SqlDeploymentStatsDao(BaseDao baseDao) {
        this.baseDao = baseDao;
    }

    /** {@inheritDoc} */
    public DeploymentStats getDeploymentStats()
    {
        try {
            List<Deployment> deployments = baseDao.geoApiNamedJbdcTemplate.query(
                    DeploymentStatsQuery.SELECT_DEPLOY_STATS.getSql(baseDao.getLogSchema()), new DeploymentStatsMapper());
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
