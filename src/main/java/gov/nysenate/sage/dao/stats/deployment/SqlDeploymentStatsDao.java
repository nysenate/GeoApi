package gov.nysenate.sage.dao.stats.deployment;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.stats.Deployment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class SqlDeploymentStatsDao implements DeploymentStatsDao {
    private final BaseDao baseDao;

    @Autowired
    public SqlDeploymentStatsDao(BaseDao baseDao) {
        this.baseDao = baseDao;
    }

    /** {@inheritDoc} */
    public List<Deployment> getDeploymentStats() {
        return baseDao.geoApiNamedJbdcTemplate.query(
                DeploymentStatsQuery.SELECT_DEPLOY_STATS.getSql(baseDao.getLogSchema()), new DeploymentStatsMapper());
    }

    private static class DeploymentStatsMapper implements RowMapper<Deployment> {
        public Deployment mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Deployment(rs.getInt("id"), rs.getTimestamp("deployTime"), rs.getInt("apiRequestsSince"));
        }
    }
}
