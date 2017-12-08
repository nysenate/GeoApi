package gov.nysenate.sage.dao.stats;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.model.ApiUserDao;
import gov.nysenate.sage.model.stats.ApiUserStats;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@Repository
public class ApiUserStatsDao extends BaseDao
{
    private static Logger logger = Logger.getLogger(ApiUserStatsDao.class);
    private static ApiUserDao apiUserDao = new ApiUserDao();

    private QueryRunner run = getQueryRunner();

    public Map<Integer, ApiUserStats> getRequestCounts(Timestamp from, Timestamp to)
    {
        String requestCounts = "SELECT ar.apiUserId, COUNT(DISTINCT ar.id) AS apiRequests, \n" +
                "                          COUNT(DISTINCT gr.id) AS geoRequests,\n" +
                "                          COUNT(DISTINCT dr.id) AS distRequests\n" +
                "FROM log.apiRequest ar\n" +
                "LEFT JOIN log.geocodeRequest gr ON gr.apiRequestId = ar.id\n" +
                "LEFT JOIN log.districtRequest dr ON dr.apiRequestId = ar.id\n" +
                "WHERE ar.requestTime >= ? AND ar.requestTime <= ?\n" +
                "GROUP BY ar.apiUserId";

        String methodCounts = "SELECT ar.apiUserId, s.name AS service, rt.name AS method, COUNT(*) AS requests\n" +
                "FROM log.apiRequest ar\n" +
                "LEFT JOIN log.requestTypes rt ON ar.requestTypeId = rt.Id\n" +
                "LEFT JOIN log.services s ON rt.serviceId = s.id\n" +
                "WHERE ar.requestTime >= ? AND ar.requestTime <= ?\n" +
                "GROUP BY ar.apiUserId, s.name, rt.name\n" +
                "ORDER BY ar.apiUserId, service, method";
        try {
            Map<Integer, ApiUserStats> apiUserStatsMap = run.query(requestCounts, new RequestCountHandler(), from, to);
            return run.query(methodCounts, new MethodRequestCountHandler(apiUserStatsMap), from, to);
        }
        catch (SQLException ex) {
            logger.error("Failed to get ApiUser stats!", ex);
        }
        return null;
    }

    public static class MethodRequestCountHandler implements ResultSetHandler<Map<Integer, ApiUserStats>>
    {
        Map<Integer, ApiUserStats> apiUserStatsMap;
        public MethodRequestCountHandler(Map<Integer, ApiUserStats> apiUserStatsMap) {
            this.apiUserStatsMap = apiUserStatsMap;
        }

        @Override
        public Map<Integer, ApiUserStats> handle(ResultSet rs) throws SQLException {
            while (rs.next()) {
                Integer apiUserId = rs.getInt("apiUserId");
                if (this.apiUserStatsMap.get(apiUserId) != null) {
                    this.apiUserStatsMap.get(apiUserId).addMethodRequestCount(rs.getString("service"), rs.getString("method"), rs.getInt("requests"));
                }
            }
            return this.apiUserStatsMap;
        }
    }

    public static class RequestCountHandler implements ResultSetHandler<Map<Integer, ApiUserStats>>
    {
        @Override
        public Map<Integer, ApiUserStats> handle(ResultSet rs) throws SQLException {
            Map<Integer, ApiUserStats> requestCountMap = new HashMap<>();
            while (rs.next()) {
                ApiUserStats apiUserStats = new ApiUserStats();
                apiUserStats.setApiUser(apiUserDao.getApiUserById(rs.getInt("apiUserId")));
                apiUserStats.setApiRequests(rs.getInt("apiRequests"));
                apiUserStats.setGeoRequests(rs.getInt("geoRequests"));
                apiUserStats.setDistRequests(rs.getInt("distRequests"));
                requestCountMap.put(rs.getInt("apiUserId"),apiUserStats);
            }
            return requestCountMap;
        }
    }
}
