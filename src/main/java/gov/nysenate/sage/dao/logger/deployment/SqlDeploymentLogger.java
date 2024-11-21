package gov.nysenate.sage.dao.logger.deployment;

import gov.nysenate.sage.dao.base.BaseDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

@Repository
public class SqlDeploymentLogger implements DeploymentLogger {
    private final BaseDao baseDao;

    @Autowired
    public SqlDeploymentLogger(BaseDao baseDao) {
        this.baseDao = baseDao;
    }

    /**
     * {@inheritDoc}
     */
    public void logDeploymentStatus() {
        baseDao.geoApiNamedJbdcTemplate.update(
                DeploymentQuery.INSERT_DEPLOYMENT.getSql(baseDao.getLogSchema()),  new MapSqlParameterSource());
    }
}
