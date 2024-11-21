package gov.nysenate.sage.dao.logger.apirequest;

import gov.nysenate.sage.model.api.ApiRequest;

public interface ApiRequestLogger {
    /**
     * Log an Api request to the database.
     * @param apiRequest ApiRequest to log
     * @return int id of ApiRequest
     */
    int logApiRequest(ApiRequest apiRequest);
}
