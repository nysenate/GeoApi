package gov.nysenate.sage.dao.logger.apirequest;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.api.ApiRequest;
import gov.nysenate.sage.model.api.ApiUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class SqlApiRequestLogger implements ApiRequestLogger
{
    private static Logger logger = LoggerFactory.getLogger(SqlApiRequestLogger.class);
    private BaseDao baseDao;

    @Autowired
    public SqlApiRequestLogger(BaseDao baseDao) {
        this.baseDao = baseDao;
    }

    /** {@inheritDoc} */
    public int logApiRequest(ApiRequest apiRequest)
    {
        if (apiRequest != null) {
            ApiUser apiUser = apiRequest.getApiUser();
            try {
                MapSqlParameterSource params = new MapSqlParameterSource();
                params.addValue("ipAddress", apiRequest.getIpAddress().getHostAddress());
                params.addValue("apiUserId", apiUser.getId());
                params.addValue("version",2);
                params.addValue("requestTime", apiRequest.getApiRequestTime());
                params.addValue("isBatch",apiRequest.isBatch());
                params.addValue("requestTypeName",apiRequest.getRequest());
                params.addValue("serviceName", apiRequest.getService());

                List<Integer> idList = baseDao.geoApiNamedJbdcTemplate.query(
                        ApiRequestQuery.INSERT_API_REQUEST.getSql(baseDao.getLogSchema()), params, new ApiRequestIdHandler());
                int id = idList.get(0);
                logger.debug("Saved apiRequest " + id + " to log");
                return id;
            }
            catch (Exception ex) {
                logger.error("Failed to log Api Request into the database", ex);
            }
        }
        return 0;
    }

    /** {@inheritDoc} */
    public ApiRequest getApiRequest(int apiRequestId) {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("apiRequestId", apiRequestId);

            List<ApiRequest> apiRequestList =  baseDao.geoApiNamedJbdcTemplate.query(
                    ApiRequestQuery.GET_API_REQUEST.getSql(baseDao.getLogSchema()), params,
                    new ListApiRequestResultHandler());
            if (!apiRequestList.isEmpty()) {
                return apiRequestList.get(0);
            }
        }
        catch (Exception ex) {
            logger.error("Failed to retrieve ApiRequest by id!", ex);
        }
        return null;
    }

    private static class ListApiRequestResultHandler implements RowMapper<ApiRequest> {

        @Override
        public ApiRequest mapRow(ResultSet rs, int rowNum) throws SQLException {
                ApiRequest ar = new ApiRequest();
                ar.setId(rs.getInt("requestId"));
                ApiUser au = new ApiUser(rs.getInt("apiUserId"), rs.getString("apiKey"),
                                         rs.getString("apiUserName"), rs.getString("apiUserDesc"), rs.getBoolean("admin"));
                ar.setApiUser(au);
                ar.setService(rs.getString("service"));
                ar.setRequest(rs.getString("request"));
                ar.setApiRequestTime(rs.getTimestamp("requestTime"));
                ar.setVersion(rs.getInt("version"));
                ar.setBatch(rs.getBoolean("isBatch"));
                return ar;
        }
    }

    private static class ApiRequestIdHandler implements RowMapper<Integer> {
        @Override
        public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getInt("id");
        }
    }
}