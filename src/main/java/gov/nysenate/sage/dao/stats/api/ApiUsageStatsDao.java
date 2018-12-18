package gov.nysenate.sage.dao.stats.api;

import gov.nysenate.sage.model.stats.ApiUsageStats;

import java.sql.Timestamp;

public interface ApiUsageStatsDao {

    /**
     * Retrieve Api Usage Stats within a specified time frame.
     * @return ApiUsageStats
     */
    public ApiUsageStats getApiUsageStats(Timestamp from, Timestamp to, SqlApiUsageStatsDao.RequestInterval requestInterval);
}
