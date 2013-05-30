package gov.nysenate.sage.dao.log;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.api.ApiRequest;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.log4j.Logger;

import java.sql.Timestamp;
import java.util.List;

public class ApiRequestLogger extends BaseDao
{
    private static Logger logger = Logger.getLogger(ApiRequestLogger.class);
    private QueryRunner runner = getQueryRunner();

    /**
     * Log an Api request to the database.
     * @param apiRequest ApiRequest to log
     */
    public void logApiRequest(ApiRequest apiRequest)
    {

    }

    /**
     * Retrieve logged Api requests.
     * @param apiKey         Api Key to search for. If null or blank, search for all Api keys.
     * @param method         Method to filter by. If null or blank, search for all methods.
     * @param limit          Limit results. If -1, return all results.
     * @param orderByRecent  If true, sort by most recent first. Otherwise return least recent first.
     * @return
     */
    public List<ApiRequest> getApiRequests(String apiKey, String method, int limit, boolean orderByRecent)
    {
        return null;
    }

    /**
     * Retrieve logged Api requests within a specified time frame.
     * @param apiKey         Api Key to search for. If null or blank, search for all Api keys.
     * @param method         Method to filter by. If null or blank, search for all methods.
     * @param from           Inclusive start date/time.
     * @param to             Inclusive end date/time.
     * @param limit          Limit results. If -1, return all results.
     * @param orderByRecent  If true, sort by most recent first. Otherwise return least recent first.
     * @return
     */
    public List<ApiRequest> getApiRequestsDuring(String apiKey, String method, Timestamp from, Timestamp to, int limit,
                                                 boolean orderByRecent)
    {
        return null;
    }
}