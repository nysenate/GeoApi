package gov.nysenate.sage.dao.logger.apirequest;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.api.ApiRequest;
import gov.nysenate.sage.model.api.ApiUser;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class SqlApiRequestLogger extends BaseDao {
    /**
     * Log an Api request to the database.
     * @param apiRequest ApiRequest to log
     */
    public void logApiRequest(@Nonnull ApiRequest apiRequest) {
        ApiUser apiUser = apiRequest.getApiUser();
        var params = new MapSqlParameterSource()
                .addValue("ipAddress", apiRequest.getHostAddress())
                .addValue("apiUserId", apiUser.getId())
                .addValue("request", apiRequest.getRequest())
                .addValue("service", apiRequest.getService())
                .addValue("params", apiRequest.getParams());

        namedJdbcTemplate.query(ApiRequestQuery.INSERT_API_REQUEST.getSql(getLogSchema()),
                params, new ApiRequestIdHandler());
    }

    private static class ApiRequestIdHandler implements RowMapper<Integer> {
        @Override
        public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getInt("id");
        }
    }
}
