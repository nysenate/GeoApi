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

    public void logApiRequest(ApiRequest apiRequest)
    {

    }

    public List<ApiRequest> getApiRequests(String apiKey, String method, int limit)
    {
        return null;
    }

    public List<ApiRequest> getApiRequestsDuring(String apiKey, String method, Timestamp from, Timestamp to, int limit)
    {
        return null;
    }


}
