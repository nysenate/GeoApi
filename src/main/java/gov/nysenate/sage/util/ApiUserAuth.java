package gov.nysenate.sage.util;

import gov.nysenate.sage.model.auth.ApiUser;
import gov.nysenate.sage.dao.ApiUserDao;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.log4j.Logger;

/**
 * Provides basic key-based api authentication.
 */
public class ApiUserAuth
{
    private Logger logger = Logger.getLogger(ApiUserAuth.class);
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
