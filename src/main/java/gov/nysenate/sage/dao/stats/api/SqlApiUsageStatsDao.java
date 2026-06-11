package gov.nysenate.sage.dao.stats.api;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.stats.ApiUsageStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import static gov.nysenate.sage.model.stats.ApiUsageStats.ApiHourlyUsage;

@Repository
public class SqlApiUsageStatsDao extends BaseDao implements ApiUsageStatsDao {
    private static final Logger logger = LoggerFactory.getLogger(SqlApiUsageStatsDao.class);

    /** {@inheritDoc} */
    public ApiUsageStats getApiUsageStats(Timestamp from, Timestamp to) {
        try {
            var params = new MapSqlParameterSource("from", from)
                    .addValue("to", to);
            List<ApiHourlyUsage> usageCounts = namedJdbcTemplate.query(
                    ApiUsageStatsQuery.GET_USAGE_STATS.getSql(getLogSchema()),
                    params, new ApiUsageHandler());
            return new ApiUsageStats(from, to, usageCounts);
        }
        catch (Exception ex) {
            logger.error("Failed to get ApiUsageStats", ex);
        }
        return null;
    }

    private static class ApiUsageHandler implements RowMapper<ApiHourlyUsage> {
        @Override
        public ApiHourlyUsage mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new ApiHourlyUsage(rs.getTimestamp("request_hour"), rs.getInt("requests"));
        }
    }
}
