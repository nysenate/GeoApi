package gov.nysenate.sage.dao.stats;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.stats.ApiUsageStats;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class ApiUsageStatsDao extends BaseDao
{
    private static Logger logger = Logger.getLogger(ApiUsageStatsDao.class);
    private QueryRunner run = getQueryRunner();

    public static enum RequestInterval {
        MINUTE("minute", 1), HOUR("hour", 60), DAY("day", 1440), WEEK("week", 10080), MONTH("month", 43829), QUARTER("quarter", 131487);
        String field;
        int minutes;
        RequestInterval(String field, int minutes) {
            this.field = field;
            this.minutes = minutes;
        }
    }

    public ApiUsageStatsDao(){}

    public ApiUsageStats getApiUsageStats(Timestamp from, Timestamp to, RequestInterval requestInterval)
    {
        ApiUsageStats apiUsageStats = new ApiUsageStats();
        String sql = "SELECT date_trunc('" + requestInterval.field + "', requestTime) AS requestInterval, COUNT(*) AS requests \n" +
                     "FROM log.apiRequests AS ar \n" +
                     "WHERE ar.requestTime >= ? AND ar.requestTime <= ? \n" +
                     "GROUP BY date_trunc('" + requestInterval.field + "', requestTime)\n";
        try {
            Map<Timestamp, Integer> intervalUsageCounts = run.query(sql, new ApiUsageCountsHandler(), from, to);
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

    private class ApiUsageCountsHandler implements ResultSetHandler<Map<Timestamp, Integer>> {

        @Override
        public Map<Timestamp, Integer> handle(ResultSet rs) throws SQLException {
            Map<Timestamp, Integer> usageCounts = new HashMap<>();
            while (rs.next()) {
                usageCounts.put(rs.getTimestamp("requestInterval"), rs.getInt("requests"));
            }
            return usageCounts;
        }
    }
}
