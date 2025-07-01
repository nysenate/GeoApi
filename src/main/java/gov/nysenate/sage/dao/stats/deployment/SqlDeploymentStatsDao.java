package gov.nysenate.sage.dao.stats.deployment;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.stats.Deployment;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class SqlDeploymentStatsDao extends BaseDao implements DeploymentStatsDao {
    /** {@inheritDoc} */
    public List<Deployment> getDeploymentStats() {
        return namedJdbcTemplate.query(
                DeploymentStatsQuery.SELECT_DEPLOY_STATS.getSql(getLogSchema()), new DeploymentStatsMapper());
    }

    private static class DeploymentStatsMapper implements RowMapper<Deployment> {
        @Override
        public Deployment mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Deployment(rs.getInt("id"), rs.getTimestamp("deploy_time"), rs.getInt("api_requests_since"));
        }
    }
}
