package gov.nysenate.sage.dao.logger.deployment;

import gov.nysenate.sage.dao.base.BaseDao;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

@Repository
public class SqlDeploymentLogger extends BaseDao implements DeploymentLogger {
    public void logDeploymentStatus() {
        geoApiNamedJbdcTemplate.update(
                DeploymentQuery.INSERT_DEPLOYMENT.getSql(getLogSchema()), new MapSqlParameterSource());
    }
}
