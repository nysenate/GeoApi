package gov.nysenate.sage.dao.stats.api;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.model.api.ApiUserDao;
import gov.nysenate.sage.model.api.ApiUser;
import gov.nysenate.sage.model.stats.ApiUserStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class SqlApiUserStatsDao extends BaseDao implements ApiUserStatsDao {
    private final ApiUserDao apiUserDao;

    @Autowired
    public SqlApiUserStatsDao(ApiUserDao apiUserDao) {
        this.apiUserDao = apiUserDao;
    }

    /** {@inheritDoc} */
    public Map<Integer, ApiUserStats> getRequestCounts(Timestamp from, Timestamp to) {
        Map<Integer, ApiUser> idToUserMap = apiUserDao.getApiUsers().stream()
                .collect(Collectors.toMap(ApiUser::getId, Function.identity()));
        var params = new MapSqlParameterSource("from", from).addValue("to", to);
        var handler = new RequestCountHandler(idToUserMap);
        namedJdbcTemplate.query(ApiUserStatsQuery.GET_REQUESTS.getSql(getLogSchema()), params, handler);
        return handler.dataMap;
    }

    private record RequestCountHandler(Map<Integer, ApiUser> idToUserMap, Map<Integer, ApiUserStats> dataMap)
            implements RowCallbackHandler {

        public RequestCountHandler(Map<Integer, ApiUser> idToUserMap) {
            this(idToUserMap, new HashMap<>());
        }

        @Override
        public void processRow(@Nonnull ResultSet rs) throws SQLException {
            ApiUser user = idToUserMap.get(rs.getInt("api_user_id"));
            dataMap.computeIfAbsent(user.getId(), id -> new ApiUserStats(user));
            dataMap.get(user.getId()).addMethodRequestCount(rs.getString("service"), rs.getString("request"));
        }
    }
}
