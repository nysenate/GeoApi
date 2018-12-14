package gov.nysenate.sage.dao.logger.apirequest;

import gov.nysenate.sage.model.api.ApiRequest;

import java.sql.Timestamp;
import java.util.List;

public interface ApiRequestLogger {

    /**
     * Log an Api request to the database.
     * @param apiRequest ApiRequest to log
     * @return int id of ApiRequest
     */
    public int logApiRequest(ApiRequest apiRequest);

    /**
     * Retrieve a logged ApiRequest by id
     * @param apiRequestId
     * @return ApiRequest
     */
    public ApiRequest getApiRequest(int apiRequestId);

    /**
     * Retrieve all logged Api requests.
     * @param apiKey         Api Key to search for. If null or blank, search for all Api keys.
     * @param method         Method to filter by. If null or blank, search for all methods.
     * @param orderByRecent  If true, sort by most recent first. Otherwise return least recent first.
     * @return
     */
    public List<ApiRequest> getApiRequests(String apiKey, String service, String method, boolean orderByRecent);

    /**
     * Retrieve logged Api requests within a specified time frame.
     * @param apiKey         Api Key to search for. If null or blank, search for all Api keys.
     * @param method         Method to filter by. If null or blank, search for all methods.
     * @param from           Inclusive start date/time.
     * @param to             Inclusive end date/time.
     * @param limit          Limit results. If <= 0, return all results.
     * @param offset         Row number to start from
     * @param orderByRecent  If true, sort by most recent first. Otherwise return least recent first.
     * @return               List of ApiRequest
     */
    public List<ApiRequest> getApiRequestsDuring(String apiKey, String service, String method, Timestamp from, Timestamp to, int limit,
                                                 int offset, boolean orderByRecent);


}
