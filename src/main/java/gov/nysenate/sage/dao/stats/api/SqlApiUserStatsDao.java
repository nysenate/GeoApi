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

// TODO: redo with new API request logging
@Repository
public class SqlApiUserStatsDao extends BaseDao implements ApiUserStatsDao {
    private static final Logger logger = LoggerFactory.getLogger(SqlApiUserStatsDao.class);
    private final SqlApiUserDao sqlApiUserDao;

    @Autowired
    public SqlApiUserStatsDao(SqlApiUserDao sqlApiUserDao) {
        this.sqlApiUserDao = sqlApiUserDao;
    }

    /** {@inheritDoc} */
    public Map<Integer, ApiUserStats> getRequestCounts(Timestamp from, Timestamp to) {
        try {
            var params = new MapSqlParameterSource("from", from)
                    .addValue("to", to);
            List<Map<Integer, ApiUserStats>> apiUserStatsMapList = namedJdbcTemplate.query(
                    ApiUserStatsQuery.GET_REQUEST_COUNTS.getSql(getLogSchema()), params, new RequestCountHandler(sqlApiUserDao));
            Map<Integer, ApiUserStats> apiUserStatsMap = collapseListIntoMap(apiUserStatsMapList);
            namedJdbcTemplate.query(
                    ApiUserStatsQuery.GET_METHOD_COUNTS.getSql(getLogSchema()),
                    params, new MethodRequestCountHandler(apiUserStatsMap));
            return apiUserStatsMap;
        } catch (Exception ex) {
            logger.error("Failed to get ApiUser stats!", ex);
        }
        return null;
    }

    private static Map<Integer, ApiUserStats> collapseListIntoMap(List<Map<Integer, ApiUserStats>> apiUserStatsMapList) {
        Map<Integer, ApiUserStats> apiUserStatsMap = new HashMap<>();
        for (Map<Integer, ApiUserStats> integerApiUserStatsMap : apiUserStatsMapList) {
            apiUserStatsMap.putAll(integerApiUserStatsMap);
        }
        return apiUserStatsMap;
    }

    private record MethodRequestCountHandler(Map<Integer, ApiUserStats> apiUserStatsMap)
            implements RowMapper<Map<Integer, ApiUserStats>> {
        @Override
            public Map<Integer, ApiUserStats> mapRow(ResultSet rs, int rowNum) throws SQLException {
                while (rs.next()) {
                    Integer apiUserId = rs.getInt("apiUserId");
                    if (apiUserStatsMap.get(apiUserId) != null) {
                        apiUserStatsMap.get(apiUserId).addMethodRequestCount(rs.getString("service"), rs.getString("method"), rs.getInt("requests"));
                    }
                }
                return apiUserStatsMap;
            }
        }

    private record RequestCountHandler(SqlApiUserDao sqlApiUserDao) implements RowMapper<Map<Integer, ApiUserStats>> {
        @Override
            public Map<Integer, ApiUserStats> mapRow(ResultSet rs, int rowNum) throws SQLException {
                Map<Integer, ApiUserStats> requestCountMap = new HashMap<>();
                var apiUserStats = new ApiUserStats();
                apiUserStats.setApiUser(sqlApiUserDao.getApiUserById(rs.getInt("apiUserId")));
                apiUserStats.setApiRequests(rs.getInt("apiRequests"));
                apiUserStats.setGeoRequests(rs.getInt("geoRequests"));
                apiUserStats.setDistRequests(rs.getInt("distRequests"));
                requestCountMap.put(rs.getInt("apiUserId"), apiUserStats);
                return requestCountMap;
            }
        }
}
