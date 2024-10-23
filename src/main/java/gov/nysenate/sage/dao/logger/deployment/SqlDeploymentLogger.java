package gov.nysenate.sage.dao.logger.deployment;

import gov.nysenate.sage.dao.base.BaseDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

@Repository
public class SqlDeploymentLogger implements DeploymentLogger {
    private static final Logger logger = LoggerFactory.getLogger(SqlDeploymentLogger.class);
    private final BaseDao baseDao;

    @Autowired
    public SqlDeploymentLogger(BaseDao baseDao) {
        this.baseDao = baseDao;
    }

    /**
     * {@inheritDoc}
     */
    public void logDeploymentStatus(boolean deployed, Integer deploymentId, Timestamp deployTime) {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("deployed",deployed)
                    .addValue("refId",deploymentId)
                    .addValue("deployTime",deployTime);

            baseDao.geoApiNamedJbdcTemplate.query(
                    DeploymentQuery.INSERT_DEPLOYMENT.getSql(baseDao.getLogSchema()), params, new DeploymentIdHandler());
        }
        catch (Exception ex) {
            logger.error("Failed to log deployment");
        }
    }

    private static class DeploymentIdHandler implements RowMapper<Integer> {
        @Override
        public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getInt("id");
        }
    }
}
