package gov.nysenate.sage.dao.model.api;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.api.ApiUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * ApiUserDao provides database persistence for the ApiUser model.
 */
@Repository
public class SqlApiUserDao extends BaseDao implements ApiUserDao {
    private static final Logger logger = LoggerFactory.getLogger(SqlApiUserDao.class);

    /** {@inheritDoc} */
    public ApiUser getApiUserById(int id) {
        try {
            var params = new MapSqlParameterSource("id", id);
            List<ApiUser> apiUserList = geoApiNamedJbdcTemplate.query(
                    ApiUserQuery.GET_API_USER_BY_ID.getSql(getPublicSchema()), params, new ApiUserHandler());

            return apiUserList.get(0);
        }
        catch (Exception sqlEx) {
            logger.error("Failed to get ApiUser by id in ApiUserDAO!");
            logger.error(sqlEx.getMessage());
        }
        return null;
    }

    /** {@inheritDoc} */
    public ApiUser getApiUserByKey(String key) {
        try {
            var params = new MapSqlParameterSource("apikey", key);
            List<ApiUser> apiUserList = geoApiNamedJbdcTemplate.query(
                    ApiUserQuery.GET_API_USER_BY_KEY.getSql(getPublicSchema()), params, new ApiUserHandler());
            return apiUserList.get(0);
        }
        catch (Exception sqlEx) {
            logger.error("Failed to get ApiUser by key in ApiUserDAO!");
            logger.error(sqlEx.getMessage());
        }
        return null;
    }

    /** {@inheritDoc} */
    public List<ApiUser> getApiUsers() {
        try {
            return geoApiNamedJbdcTemplate.query(
                    ApiUserQuery.GET_ALL_API_USERS.getSql(getPublicSchema()), new ApiUserHandler());
        }
        catch (Exception sqlEx) {
            logger.error("Failed to get ApiUsers!");
            logger.error(sqlEx.getMessage());
        }
        return null;
    }

    /** {@inheritDoc} */
    public int addApiUser(ApiUser apiUser) {
        try {
            var params = new MapSqlParameterSource("apikey",  apiUser.getApiKey())
                    .addValue("name",  apiUser.getName())
                    .addValue("description",  apiUser.getDescription())
                    .addValue("admin", apiUser.isAdmin());

            return geoApiNamedJbdcTemplate.update(
                    ApiUserQuery.INSERT_API_USER.getSql(getPublicSchema()), params);
        }
        catch (Exception sqlEx) {
            logger.error("Failed to add ApiUser in ApiUserDAO!");
            logger.error(sqlEx.getMessage());
        }
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public void removeApiUser(ApiUser apiUser) {
        try {
            var params = new MapSqlParameterSource("id", apiUser.getId());
            geoApiNamedJbdcTemplate.update(
                    ApiUserQuery.REMOVE_API_USER.getSql(getPublicSchema()), params);
        }
        catch (Exception sqlEx) {
            logger.error("Failed to remove ApiUser in ApiUserDAO!");
            logger.error(sqlEx.getMessage());
        }
    }

    private static class ApiUserHandler implements RowMapper<ApiUser> {
        @Override
        public ApiUser mapRow(ResultSet rs, int rowNum) throws SQLException {
            var apiUser = new ApiUser();
            apiUser.setId(rs.getInt("id"));
            apiUser.setApiKey(rs.getString("apikey"));
            apiUser.setName(rs.getString("name"));
            apiUser.setDescription(rs.getString("description"));
            apiUser.setAdmin(rs.getBoolean("admin"));
            return apiUser;
        }
    }
}
