package gov.nysenate.sage.dao.model.api;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.api.ApiUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * ApiUserDao provides database persistence for the ApiUser model.
 */
@Repository
public class SqlApiUserDao implements ApiUserDao
{
    private Logger logger = LoggerFactory.getLogger(SqlApiUserDao.class);
    private BaseDao baseDao;

    @Autowired
    public SqlApiUserDao(BaseDao baseDao) {
        this.baseDao = baseDao;
    }

    /** {@inheritDoc} */
    public ApiUser getApiUserById(int id)
    {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("id", id);

            List<ApiUser> apiUserList = baseDao.geoApiNamedJbdcTemaplate.query(
                    ApiUserQuery.GET_API_USER_BY_ID.getSql(baseDao.getPublicSchema()), params, new ApiUserHandler());

            if (apiUserList != null) {
                return apiUserList.get(0);
            }
        }
        catch (Exception sqlEx) {
            logger.error("Failed to get ApiUser by id in ApiUserDAO!");
            logger.error(sqlEx.getMessage());
        }
        return null;
    }

    /** {@inheritDoc} */
    public ApiUser getApiUserByName(String name)
    {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("name", name);

            List<ApiUser> apiUserList = baseDao.geoApiNamedJbdcTemaplate.query(
                    ApiUserQuery.GET_API_USER_BY_NAME.getSql(baseDao.getPublicSchema()), params, new ApiUserHandler());

            if (apiUserList != null) {
                return apiUserList.get(0);
            }
        }
        catch (Exception sqlEx)         {
            logger.error("Failed to get ApiUser by name in ApiUserDAO!");
            logger.error(sqlEx.getMessage());
        }
        return null;
    }

    /** {@inheritDoc} */
    public ApiUser getApiUserByKey(String key)
    {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("apikey", key);

            List<ApiUser> apiUserList = baseDao.geoApiNamedJbdcTemaplate.query(ApiUserQuery.GET_API_USER_BY_KEY.getSql(baseDao.getPublicSchema()), params, new ApiUserHandler());

            if (apiUserList != null) {
                return apiUserList.get(0);
            }
        }
        catch (Exception sqlEx) {
            logger.error("Failed to get ApiUser by key in ApiUserDAO!");
            logger.error(sqlEx.getMessage());
        }
        return null;
    }

    /** {@inheritDoc} */
    public List<ApiUser> getApiUsers()
    {
        try {
            return baseDao.geoApiNamedJbdcTemaplate.query(
                    ApiUserQuery.GET_ALL_API_USERS.getSql(baseDao.getPublicSchema()), new ApiUserHandler());
        }
        catch (Exception sqlEx) {
            logger.error("Failed to get ApiUsers!");
            logger.error(sqlEx.getMessage());
        }
        return null;
    }

    /** {@inheritDoc} */
    public int addApiUser(ApiUser apiUser)
    {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("apikey",  apiUser.getApiKey());
            params.addValue("name",  apiUser.getName());
            params.addValue("description",  apiUser.getDescription());

            return baseDao.geoApiNamedJbdcTemaplate.update(
                    ApiUserQuery.INSERT_API_USER.getSql(baseDao.getPublicSchema()), params);
        }
        catch (Exception sqlEx) {
            logger.error("Failed to add ApiUser in ApiUserDAO!");
            logger.error(sqlEx.getMessage());
        }
        return 0;
    }

    /** {@inheritDoc} */
    public int removeApiUser(ApiUser apiUser)
    {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("id", apiUser.getId());

            return baseDao.geoApiNamedJbdcTemaplate.update(
                    ApiUserQuery.REMOVE_API_USER.getSql(baseDao.getPublicSchema()), params);
        }
        catch (Exception sqlEx) {
            logger.error("Failed to remove ApiUser in ApiUserDAO!");
            logger.error(sqlEx.getMessage());
        }
        return 0;
    }

    private static class ApiUserHandler implements RowMapper<ApiUser> {
        @Override
        public ApiUser mapRow(ResultSet rs, int rowNum) throws SQLException {
            ApiUser apiUser = new ApiUser();
            apiUser.setId(rs.getInt("id"));
            apiUser.setApiKey(rs.getString("apikey"));
            apiUser.setName(rs.getString("name"));
            apiUser.setDescription(rs.getString("description"));
            return apiUser;
        }
    }
}
