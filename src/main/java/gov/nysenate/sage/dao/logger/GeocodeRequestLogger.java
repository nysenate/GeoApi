package gov.nysenate.sage.dao.logger;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.base.ReturnIdHandler;
import gov.nysenate.sage.model.api.ApiRequest;
import gov.nysenate.sage.model.api.GeocodeRequest;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class GeocodeRequestLogger extends BaseDao
{
    private static Logger logger = Logger.getLogger(GeocodeRequestLogger.class);
    private static AddressLogger addressLogger = new AddressLogger();
    private static String SCHEMA = "log";
    private static String TABLE = "geocodeRequests";
    private QueryRunner run = getQueryRunner();

    /**
    * Log a GeocodeRequest to the database
    * @param geoRequest
    * @return id of geocode request. This id is set to the supplied GeocodeRequest as well.
    */
    public int logGeocodeRequest(GeocodeRequest geoRequest)
    {
        if (geoRequest != null) {
            try {
                ApiRequest apiRequest = geoRequest.getApiRequest();
                int addressId = addressLogger.logAddress(geoRequest.getAddress());
                int requestId = run.query(
                          "INSERT INTO " + SCHEMA + "." + TABLE + "(apiRequestId, addressId, provider, useFallback, useCache, requestTime) \n" +
                          "VALUES (?, ?, ?, ?, ?, ?) \n" +
                          "RETURNING id", new ReturnIdHandler(), apiRequest.getId(), addressId, geoRequest.getProvider(), geoRequest.isUseFallback(),
                                                                 geoRequest.isUseCache(), geoRequest.getRequestTime());
                geoRequest.setId(requestId);
                return requestId;
            }
            catch (SQLException ex) {
                logger.error("Failed to log geocode request!", ex);
            }
        }
        return -1;
    }

    /**
    *
    * @param apiKey         Api Key to search for. If null or blank, search for all Api keys.
    * @param limit          Limit results. If -1, return all results.
    * @param orderByRecent  If true, sort by most recent first. Otherwise return least recent first.
    */
    public List<GeocodeRequest> getGeoRequests(String apiKey, int limit, boolean orderByRecent)
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
    public List<GeocodeRequest> getGeoRequestsDuring(String apiKey, Timestamp from, Timestamp to, int limit, boolean orderByRecent)
    {
        return null;
    }
}