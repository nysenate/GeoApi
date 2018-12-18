package gov.nysenate.sage.dao.stats.api;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.stats.ApiUsageStats;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static gov.nysenate.sage.model.stats.ApiUsageStats.IntervalUsage;

@Repository
public class SqlApiUsageStatsDao implements ApiUsageStatsDao
{
    private static Logger logger = LoggerFactory.getLogger(SqlApiUsageStatsDao.class);
    private QueryRunner run;
    private BaseDao baseDao;

    public static enum RequestInterval {
        MINUTE("minute", 1), HOUR("hour", 60), DAY("day", 1440), WEEK("week", 10080), MONTH("month", 43829), QUARTER("quarter", 131487);
        String field;
        int minutes;
        RequestInterval(String field, int minutes) {
            this.field = field;
            this.minutes = minutes;
        }
    }

    @Autowired
    public SqlApiUsageStatsDao(BaseDao baseDao) {
        this.baseDao = baseDao;
        run = this.baseDao.getQueryRunner();
    }

    public ApiUsageStats getApiUsageStats(Timestamp from, Timestamp to, RequestInterval requestInterval)
    {
        ApiUsageStats apiUsageStats = new ApiUsageStats();
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("from",from);
            params.addValue("to", to);
            params.addValue("requestInterval", requestInterval.field);

            List<IntervalUsage> intervalUsageCounts = baseDao.geoApiNamedJbdcTemaplate.query(
//                    sql,
                    ApiUsageStatsQuery.GET_USAGE_STATS.getSql(baseDao.getLogSchema()),
                    params, new ApiIntervalUsageHandler());



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
            return new IntervalUsage(rs.getTimestamp("requestInterval"), rs.getInt("requests"));

        }
    }
}
