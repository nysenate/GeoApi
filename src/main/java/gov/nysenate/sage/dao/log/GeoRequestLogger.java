package gov.nysenate.sage.dao.log;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.api.GeoRequest;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.log4j.Logger;

import java.sql.Timestamp;
import java.util.List;

public class GeoRequestLogger extends BaseDao
{
    private static Logger logger = Logger.getLogger(GeoRequestLogger.class);
    private QueryRunner runner = getQueryRunner();

    /**
    *
    * @param geoRequest
    */
    public void logGeoRequest(GeoRequest geoRequest)
    {

    }

    /**
    *
    * @param apiKey         Api Key to search for. If null or blank, search for all Api keys.
    * @param limit          Limit results. If -1, return all results.
    * @param orderByRecent  If true, sort by most recent first. Otherwise return least recent first.
    */
    public List<GeoRequest> getGeoRequests(String apiKey, int limit, boolean orderByRecent)
    {
        return null;
    }

    /**
     *
     * @param apiKey         Api Key to search for. If null or blank, search for all Api keys.
     * @param from           Inclusive start date/time.
     * @param to             Inclusive end date/time.
     * @param limit          Limit results. If -1, return all results.
     * @param orderByRecent  If true, sort by most recent first. Otherwise return least recent first.
     * @return
     */
    public List<GeoRequest> getGeoRequestsDuring(String apiKey, Timestamp from, Timestamp to, int limit, boolean orderByRecent)
    {
        return null;
    }
}
