package gov.nysenate.sage.util.auth;

import gov.nysenate.sage.dao.model.api.SqlApiUserDao;
import gov.nysenate.sage.model.api.ApiUser;
import gov.nysenate.sage.service.security.ApiKeyLoginToken;
import gov.nysenate.sage.util.controller.ApiControllerUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * Provides basic key-based api authentication.
 */
@Component
public class ApiUserAuth
{
    private Logger logger = LoggerFactory.getLogger(ApiUserAuth.class);
    private SqlApiUserDao sqlApiUserDao;
    @Value("${user.ip.filter:(127.0.0.1)}") private String userIpFilter;

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

    /**
     * This method ensures that the api key is of the proper length that sage generates.
     * Sage only generates api keys with the length of 32
     * @param submittedKey
     * @return
     */
    private boolean isKeyValidInput(String submittedKey) {
        return (submittedKey.length() == 32);
    }

    /**
     * Authenticate the subject using one of two authentication methods
     *  - API key authentication if a key is provided
     *  - test for ip whitelist match or existing session from ui login
     * @param subject Subject
     * @param ipAddress String
     * @param key String
     * @return boolean - true iff user was successfully authenticated
     */
    public boolean authenticateAdmin(HttpServletRequest request, Subject subject, String ipAddress, String key) {
        // Authenticate based on a key, if one is provided
        if (!StringUtils.isEmpty(key) && isKeyValidInput(key)) {
            return authenticateKey(request, subject, ipAddress, key);
        }

        // Grant access if user is in ip whitelist, or authenticated via the ui
        return !StringUtils.isEmpty(ipAddress) && ipAddress.matches(userIpFilter) &&
                subject.hasRole("ADMIN");

    }

    /**
     * Authenticate the subject using the given api key
     * @param subject Subject
     * @param ipAddress String
     * @param key String
     * @return boolean - true iff the passed in key is valid
     */
    private boolean authenticateKey(HttpServletRequest request, Subject subject, String ipAddress, String key) {
        // Return true if the user is already authenticated using the same key
        if (key.equals(subject.getPrincipal())) {
            return true;
        }
        // Validate the key and login if it is valid

        ApiUser potentialApiUser = getApiUser(key);
        if (potentialApiUser != null && potentialApiUser.isAdmin()) {
            subject.login(new ApiKeyLoginToken(key, ipAddress));
            ApiControllerUtil.setAuthenticated(request, true, potentialApiUser.getName() );
            return true;
        }
        return false;
    }
}
