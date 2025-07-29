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

import static gov.nysenate.sage.dao.model.api.ApiUserQuery.*;

/**
 * ApiUserDao provides database persistence for the ApiUser model.
 */
@Repository
public class ApiUserDao extends BaseDao {
    private static final Logger logger = LoggerFactory.getLogger(ApiUserDao.class);

    /**
     * Retrieves an ApiUser from the database by key.
     * @param id         The api user id.
     * @return ApiUser   The matched ApiUser or null if not found.
     */
    public ApiUser getApiUserById(int id) {
        return getApiUserBy("id", String.valueOf(id), GET_API_USER_BY_ID);
    }

    /**
     * Retrieves an ApiUser from the database by key.
     * @param key        The api key.
     * @return ApiUser   The matched ApiUser or null if not found.
     */
    public ApiUser getApiUserByKey(String key) {
        return getApiUserBy("apikey", key, GET_API_USER_BY_KEY);
    }

    public ApiUser getRequiredApiUser(RequiredApiUser requiredUser) {
        return getApiUserBy("name", requiredUser.name().toLowerCase(), GET_API_USER_BY_NAME);
    }

    private ApiUser getApiUserBy(String fieldName, String fieldValue, ApiUserQuery query) {
        try {
            var params = new MapSqlParameterSource(fieldName, fieldValue);
            List<ApiUser> apiUserList = namedJdbcTemplate.query(
                    query.getSql(getPublicSchema()), params, new ApiUserHandler());
            return apiUserList.get(0);
        }
        catch (Exception sqlEx) {
            logger.error("Failed to get ApiUser by {} in ApiUserDAO!", fieldName);
            logger.error(sqlEx.getMessage());
            return null;
        }
    }

    /**
     * Retrieves all ApiUsers.
     * @return      List of ApiUser
     */
    public List<ApiUser> getApiUsers() {
        try {
            return namedJdbcTemplate.query(
                    ApiUserQuery.GET_ALL_API_USERS.getSql(getPublicSchema()), new ApiUserHandler());
        }
        catch (Exception sqlEx) {
            logger.error("Failed to get ApiUsers!");
            logger.error(sqlEx.getMessage());
            return null;
        }
    }

    /**
     * Adds an API User to the database.
     * @param apiUser   The ApiUser to add.
     * @return boolean  If the user was added.
     */
    public boolean addApiUser(ApiUser apiUser) {
        try {
            var params = new MapSqlParameterSource("apikey", apiUser.getApiKey())
                    .addValue("name",  apiUser.getName())
                    .addValue("description",  apiUser.getDescription())
                    .addValue("admin", apiUser.isAdmin());

            return namedJdbcTemplate.update(
                    ApiUserQuery.INSERT_API_USER.getSql(getPublicSchema()), params) == 1;
        }
        catch (Exception sqlEx) {
            logger.error("Failed to add ApiUser in ApiUserDAO!");
            logger.error(sqlEx.getMessage());
            return false;
        }
    }

    /**
     * Removes an API User from the database.
     * @param apiUser The ApiUser to add.
     */
    public void removeApiUser(ApiUser apiUser) {
        try {
            var params = new MapSqlParameterSource("id", apiUser.getId());
            namedJdbcTemplate.update(
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
            var apiUser = new ApiUser(rs.getString("apikey"), rs.getString("name"),
                    rs.getString("description"), rs.getBoolean("admin"));
            apiUser.setId(rs.getInt("id"));
            return apiUser;
        }
    }
}
