package gov.nysenate.sage.dao.stats;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.stats.ApiUsageStats;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.springframework.beans.factory.annotation.Autowired;
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
public class ApiUsageStatsDao
{
    private static Logger logger = LoggerFactory.getLogger(ApiUsageStatsDao.class);
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
    public ApiUsageStatsDao(BaseDao baseDao) {
        this.baseDao = baseDao;
        run = this.baseDao.getQueryRunner();
    }

    public ApiUsageStats getApiUsageStats(Timestamp from, Timestamp to, RequestInterval requestInterval)
    {
        ApiUsageStats apiUsageStats = new ApiUsageStats();
        String sql = "SELECT date_trunc('" + requestInterval.field + "', requestTime) AS requestInterval, COUNT(*) AS requests \n" +
                     "FROM log.apiRequest AS ar \n" +
                     "WHERE ar.requestTime >= ? AND ar.requestTime <= ? \n" +
                     "GROUP BY date_trunc('" + requestInterval.field + "', requestTime)\n" +
                     "ORDER BY requestInterval";
        try {
            List<IntervalUsage> intervalUsageCounts = run.query(sql, new ApiIntervalUsageHandler(), from, to);
            apiUsageStats.setIntervalSizeInMinutes(requestInterval.minutes);
            apiUsageStats.setIntervalFrom(from);
            apiUsageStats.setIntervalTo(to);
            apiUsageStats.setIntervalUsageCounts(intervalUsageCounts);
            return apiUsageStats;
        }
        catch (SQLException ex) {
            logger.error("Failed to get ApiUsageStats", ex);
        }
        return null;
    }

    private static class ApiIntervalUsageHandler implements ResultSetHandler<List<IntervalUsage>> {

        @Override
        public List<IntervalUsage> handle(ResultSet rs) throws SQLException {
            List<IntervalUsage> usageCounts = new ArrayList<>();
            while (rs.next()) {
                usageCounts.add(new IntervalUsage(rs.getTimestamp("requestInterval"), rs.getInt("requests")));
            }
            return usageCounts;
        }
    }
}
