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
import java.util.List;

@Repository
public class SqlDeploymentLogger implements DeploymentLogger
{
    private static Logger logger = LoggerFactory.getLogger(SqlDeploymentLogger.class);
    private BaseDao baseDao;

    @Autowired
    public SqlDeploymentLogger(BaseDao baseDao) {
        this.baseDao = baseDao;
    }

    /** {@inheritDoc} */
    public Integer logDeploymentStatus(boolean deployed, Integer deploymentId, Timestamp deployTime)
    {
        try {

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("deployed",deployed);
            params.addValue("refId",deploymentId);
            params.addValue("deployTime",deployTime);

            List<Integer> idList = baseDao.geoApiNamedJbdcTemplate.query(
                    DeploymentQuery.INSERT_DEPLOYMENT.getSql(baseDao.getLogSchema()), params, new DeploymentIdHandler());
            if (idList == null || idList.size() <= 0) {
                return 0;
            }
            return  idList.get(0);
        }
        catch (Exception ex) {
            logger.error("Failed to log deployment");
        }
        return 0;
    }

    private static class DeploymentIdHandler implements RowMapper<Integer> {
        @Override
        public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getInt("id");
        }
    }
}
