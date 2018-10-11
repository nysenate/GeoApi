package gov.nysenate.sage.util.auth;

import gov.nysenate.sage.dao.model.ApiUserDao;
import gov.nysenate.sage.model.api.ApiUser;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * Provides basic key-based api authentication.
 */
public class ApiUserAuth
{
    private Logger logger = LoggerFactory.getLogger(ApiUserAuth.class);
    private ApiUserDao apiUserDao;

    public ApiUserAuth()
    {
        apiUserDao = new ApiUserDao();
    }

    /**
     * Retrieves the ApiUser that matches the given apiKey.
     * @param apiKey
     * @return  ApiUser if found, null otherwise
     */
    public ApiUser getApiUser(String apiKey)
    {
        return apiUserDao.getApiUserByKey(apiKey);
    }

    /**
     * Adds a new Api user to the database.
     * @param name          Name of the user
     * @param description   Description of the user
     * @return ApiUser      If success returns a new ApiUser with id and apikey.
     *                      Upon failure, null is returned.
     */
    public ApiUser addApiUser(String name, String description)
    {
        ApiUser apiUser = new ApiUser();
        apiUser.setName(name);
        apiUser.setDescription(description);
        apiUser.setApiKey(this.generateRandomKey());

        if (apiUserDao.addApiUser(apiUser) == 1)
        {
            return apiUserDao.getApiUserByKey(apiUser.getApiKey());
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
