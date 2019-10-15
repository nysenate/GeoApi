package gov.nysenate.sage.util.auth;

import gov.nysenate.sage.dao.model.api.SqlApiUserDao;
import gov.nysenate.sage.model.api.ApiUser;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Provides basic key-based api authentication.
 */
@Component
public class ApiUserAuth
{
    private Logger logger = LoggerFactory.getLogger(ApiUserAuth.class);
    private SqlApiUserDao sqlApiUserDao;

    @Autowired
    public ApiUserAuth(SqlApiUserDao sqlApiUserDao)
    {
        this.sqlApiUserDao = sqlApiUserDao;
    }

    /**
     * Retrieves the ApiUser that matches the given apiKey.
     * @param apiKey
     * @return  ApiUser if found, null otherwise
     */
    public ApiUser getApiUser(String apiKey)
    {
        return sqlApiUserDao.getApiUserByKey(apiKey);
    }

    /**
     * Adds a new Api user to the database.
     * @param name          Name of the user
     * @param description   Description of the user
     * @return ApiUser      If success returns a new ApiUser with id and apikey.
     *                      Upon failure, null is returned.
     */
    public ApiUser addApiUser(String name, String description, boolean admin)
    {
        ApiUser apiUser = new ApiUser();
        apiUser.setName(name);
        apiUser.setDescription(description);
        apiUser.setApiKey(this.generateRandomKey());
        apiUser.setAdmin(admin);

        if (sqlApiUserDao.addApiUser(apiUser) == 1)
        {
            return sqlApiUserDao.getApiUserByKey(apiUser.getApiKey());
        }

        return null;
    }

    /**
     * Generates and returns a random 32 character key.
     * @return String   key
     */
    private String generateRandomKey()
    {
        return RandomStringUtils.randomAlphanumeric(32);
    }
}
