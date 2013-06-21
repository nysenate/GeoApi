package gov.nysenate.sage.dao.logger;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.base.ReturnIdHandler;
import gov.nysenate.sage.model.api.ApiRequest;
import gov.nysenate.sage.model.api.ApiUser;
import gov.nysenate.sage.util.FormatUtil;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ApiRequestLogger extends BaseDao
{

    private static Logger logger = Logger.getLogger(ApiRequestLogger.class);
    private static String SCHEMA = "log";
    private static String TABLE = "apiRequests";
    private QueryRunner run = getQueryRunner();

    /**
     * Log an Api request to the database.
     * @param apiRequest ApiRequest to log
     * @return int id of ApiRequest
     */
    public int logApiRequest(ApiRequest apiRequest)
    {
        if (apiRequest != null) {
            ApiUser apiUser = apiRequest.getApiUser();
            String sql = "INSERT INTO " + SCHEMA + "." + TABLE + "(ipAddress, apiUserId, version, requestTypeId, requestTime) \n" +
                    "SELECT inet '" + apiRequest.getIpAddress().getHostAddress() + "', ?, 2, rt.id, now() \n" +
                    "FROM log.requestTypes AS rt \n" +
                    "LEFT JOIN log.services ser ON rt.serviceId = ser.id \n" +
                    "WHERE rt.name = ? AND ser.name = ?\n" +
                    "RETURNING id";
            try {
                int id = run.query(sql, new ReturnIdHandler(), apiUser.getId(), apiRequest.getRequest(), apiRequest.getService());
                logger.debug("Saved apiRequest " + id + " to log");
                return id;
            }
            catch (SQLException ex) {
                logger.error("Failed to log Api Request into the database", ex);
            }
        }
        return 0;
    }

    /**
     * Retrieve all logged Api requests.
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
    public List<ApiRequest> getApiRequestsDuring(String apiKey, String service, String method, Timestamp from, Timestamp to, int limit,
                                                 int offset, boolean orderByRecent)
    {
        String sql = "SELECT log.apiRequests.id AS requestId, ipAddress, version, serv.name AS service, rt.name AS request, requestTime, \n" +
                     "       au.id AS apiUserId, au.name AS apiUserName, au.apiKey AS apiKey, au.description AS apiUserDesc \n" +
                     "FROM " + SCHEMA + "." + TABLE + "\n" +
                     "LEFT JOIN " + "public.apiUser au ON apiUserId = au.id \n" +
                     "LEFT JOIN " + SCHEMA + ".requestTypes rt ON requestTypeId = rt.id \n" +
                     "LEFT JOIN " + SCHEMA + ".services serv ON rt.serviceId = serv.id \n" +
                     "WHERE requestTime >= ? AND requestTime <= ? \n" +
                     "AND CASE WHEN ? != '' THEN rt.name = ? ELSE true END\n" +
                     "AND CASE WHEN ? != '' THEN serv.name = ? ELSE true END\n" +
                     "LIMIT ? OFFSET ?";
        try {
            return run.query(sql, new ListApiRequestResultHandler(), from, to, service, service, method, method, limit, offset);
        }
        catch (SQLException ex) {
            logger.error("Failed to retrieve logged api requests!", ex);
        }
        return null;
    }

    private static class ListApiRequestResultHandler implements ResultSetHandler<List<ApiRequest>> {

        @Override
        public List<ApiRequest> handle(ResultSet rs) throws SQLException {
            List<ApiRequest> apiRequests = new ArrayList<>();
            while (rs.next()) {
                ApiRequest ar = new ApiRequest();
                ar.setId(rs.getInt("requestId"));
                ApiUser au = new ApiUser(rs.getInt("apiUserId"), rs.getString("apiKey"),
                                         rs.getString("apiUserName"), rs.getString("apiUserDesc"));
                ar.setApiUser(au);
                ar.setService(rs.getString("service"));
                ar.setRequest(rs.getString("request"));
                ar.setApiRequestTime(rs.getTimestamp("requestTime"));
                ar.setVersion(rs.getInt("version"));
                apiRequests.add(ar);
            }
            return apiRequests;
        }
    }


}