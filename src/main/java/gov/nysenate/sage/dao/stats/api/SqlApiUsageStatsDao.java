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

import static gov.nysenate.sage.model.stats.ApiUsageStats.IntervalUsage;

@Repository
public class SqlApiUsageStatsDao extends BaseDao implements ApiUsageStatsDao {
    private static final Logger logger = LoggerFactory.getLogger(SqlApiUsageStatsDao.class);

    public enum RequestInterval {
        MINUTE(1), HOUR(60), DAY(24*60),
        WEEK(7*24*60), MONTH(43829), QUARTER(131487);
        private final int minutes;

        RequestInterval(int minutes) {
            this.minutes = minutes;
        }
    }

    /** {@inheritDoc} */
    public ApiUsageStats getApiUsageStats(Timestamp from, Timestamp to, String intervalStr) {
        RequestInterval requestInterval = RequestInterval.valueOf(intervalStr.toUpperCase());
        try {
            var params = new MapSqlParameterSource("from", from)
                    .addValue("to", to)
                    .addValue("requestInterval", requestInterval.name().toLowerCase());
            List<IntervalUsage> intervalUsageCounts = namedJdbcTemplate.query(
                    ApiUsageStatsQuery.GET_USAGE_STATS.getSql(getLogSchema()),
                    params, new ApiIntervalUsageHandler());

            var apiUsageStats = new ApiUsageStats();
            apiUsageStats.setIntervalSizeInMinutes(requestInterval.minutes);
            apiUsageStats.setIntervalFrom(from);
            apiUsageStats.setIntervalTo(to);
            apiUsageStats.setIntervalUsageCounts(intervalUsageCounts);
            return apiUsageStats;
        }
        catch (Exception ex) {
            logger.error("Failed to get ApiUsageStats", ex);
        }
        return null;
    }

    private static class ApiIntervalUsageHandler implements RowMapper<IntervalUsage> {
        @Override
        public IntervalUsage mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new IntervalUsage(rs.getTimestamp("request_interval"), rs.getInt("requests"));
        }
    }
}
