package gov.nysenate.sage.dao.stats.api;

import gov.nysenate.sage.model.stats.ApiUserStats;

import java.sql.Timestamp;
import java.util.Map;

public interface ApiUserStatsDao {


    /**
     * Retrieve Api User Stats within a specified time frame.
     * @return Map<Integer, ApiUserStats>
     */
    public Map<Integer, ApiUserStats> getRequestCounts(Timestamp from, Timestamp to);
}
