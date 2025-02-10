package gov.nysenate.sage.dao.logger.apirequest;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.api.ApiRequest;
import gov.nysenate.sage.model.api.ApiUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class SqlApiRequestLogger extends BaseDao implements ApiRequestLogger {
    private static final Logger logger = LoggerFactory.getLogger(SqlApiRequestLogger.class);

    /** {@inheritDoc} */
    public int logApiRequest(ApiRequest apiRequest) {
        if (apiRequest != null) {
            ApiUser apiUser = apiRequest.getApiUser();
            try {
                var params = new MapSqlParameterSource()
                        .addValue("ipAddress", apiRequest.getIpAddress().getHostAddress())
                        .addValue("apiUserId", apiUser.getId())
                        .addValue("requestTime", apiRequest.getApiRequestTime())
                        .addValue("isBatch", apiRequest.isBatch())
                        .addValue("requestTypeName",apiRequest.getRequest())
                        .addValue("serviceName", apiRequest.getService());

                List<Integer> idList = namedJdbcTemplate.query(
                        ApiRequestQuery.INSERT_API_REQUEST.getSql(getLogSchema()), params, new ApiRequestIdHandler());
                return idList.get(0);
            }
            catch (Exception ex) {
                // TODO
                logger.error("Failed to log Api Request into the database");
            }
        }
        return 0;
    }

    private static class ApiRequestIdHandler implements RowMapper<Integer> {
        @Override
        public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getInt("id");
        }
    }
}
