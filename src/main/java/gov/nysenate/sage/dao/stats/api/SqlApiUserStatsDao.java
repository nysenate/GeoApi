package gov.nysenate.sage.dao.stats.api;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.model.api.SqlApiUserDao;
import gov.nysenate.sage.model.stats.ApiUserStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class SqlApiUserStatsDao implements ApiUserStatsDao {
    private static Logger logger = LoggerFactory.getLogger(SqlApiUserStatsDao.class);
    private SqlApiUserDao sqlApiUserDao;
    private BaseDao baseDao;

    @Autowired
    public SqlApiUserStatsDao(SqlApiUserDao sqlApiUserDao, BaseDao baseDao) {
        this.sqlApiUserDao = sqlApiUserDao;
        this.baseDao = baseDao;
    }

    /** {@inheritDoc} */
    public Map<Integer, ApiUserStats> getRequestCounts(Timestamp from, Timestamp to) {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("from", from);
            params.addValue("to", to);


            List<Map<Integer, ApiUserStats>> apiUserStatsMapList = baseDao.geoApiNamedJbdcTemplate.query(
                    ApiUserStatsQuery.GET_REQUEST_COUNTS.getSql(baseDao.getLogSchema()), params ,new RequestCountHandler(sqlApiUserDao));

            Map<Integer, ApiUserStats> apiUserStatsMap = collapseListIntoMap(apiUserStatsMapList);



            baseDao.geoApiNamedJbdcTemplate.query(
                    ApiUserStatsQuery.GET_METHOD_COUNTS.getSql(baseDao.getLogSchema()),
                    params ,new MethodRequestCountHandler(apiUserStatsMap));

            return apiUserStatsMap;
        } catch (Exception ex) {
            logger.error("Failed to get ApiUser stats!", ex);
        }
        return null;
    }

    private Map<Integer, ApiUserStats> collapseListIntoMap(List<Map<Integer, ApiUserStats>> apiUserStatsMapList) {
        Map<Integer, ApiUserStats> apiUserStatsMap = new HashMap<>();
        for (Map<Integer, ApiUserStats> integerApiUserStatsMap : apiUserStatsMapList) {
            apiUserStatsMap.putAll(integerApiUserStatsMap);
        }
        return apiUserStatsMap;
    }

    public static class MethodRequestCountHandler implements RowMapper<Map<Integer, ApiUserStats>> {
        Map<Integer, ApiUserStats> apiUserStatsMap;

        public MethodRequestCountHandler(Map<Integer, ApiUserStats> apiUserStatsMap) {
            this.apiUserStatsMap = apiUserStatsMap;
        }

        public Map<Integer, ApiUserStats> mapRow(ResultSet rs, int rowNum) throws SQLException {
            while (rs.next()) {
                Integer apiUserId = rs.getInt("apiUserId");
                if (this.apiUserStatsMap.get(apiUserId) != null) {
                    this.apiUserStatsMap.get(apiUserId).addMethodRequestCount(rs.getString("service"), rs.getString("method"), rs.getInt("requests"));
                }
            }
            return this.apiUserStatsMap;
        }
    }

    public static class RequestCountHandler implements RowMapper<Map<Integer, ApiUserStats>> {
        private SqlApiUserDao sqlApiUserDao;

        public RequestCountHandler(SqlApiUserDao sqlApiUserDao) {
            this.sqlApiUserDao = sqlApiUserDao;
        }

        public Map<Integer, ApiUserStats> mapRow(ResultSet rs, int rowNum) throws SQLException {
            Map<Integer, ApiUserStats> requestCountMap = new HashMap<>();
            ApiUserStats apiUserStats = new ApiUserStats();
            apiUserStats.setApiUser(sqlApiUserDao.getApiUserById(rs.getInt("apiUserId")));
            apiUserStats.setApiRequests(rs.getInt("apiRequests"));
            apiUserStats.setGeoRequests(rs.getInt("geoRequests"));
            apiUserStats.setDistRequests(rs.getInt("distRequests"));
            requestCountMap.put(rs.getInt("apiUserId"), apiUserStats);
            return requestCountMap;
        }
    }
}
