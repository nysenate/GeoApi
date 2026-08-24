package gov.nysenate.sage.util.auth;

import gov.nysenate.sage.dao.model.api.ApiUserDao;
import gov.nysenate.sage.dao.model.api.RequiredApiUser;
import gov.nysenate.sage.model.api.ApiUser;
import gov.nysenate.sage.service.security.ApiKeyLoginToken;
import gov.nysenate.sage.util.controller.ApiControllerUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Provides basic key-based api authentication.
 */
@Component
public class ApiUserAuth {
    private final ApiUserDao apiUserDao;
    @Value("${user.ip.filter:(127.0.0.1)}")
    private String userIpFilter;

    @Autowired
    public ApiUserAuth(ApiUserDao apiUserDao) {
        this.apiUserDao = apiUserDao;
    }

    @PostConstruct
    private void init() {
        Set<String> apiUserNames = apiUserDao.getApiUsers().stream().map(ApiUser::getName)
                .collect(Collectors.toSet());
        for (RequiredApiUser user : RequiredApiUser.values()) {
            if (!apiUserNames.contains(user.name().toLowerCase())) {
                addApiUser(user.name().toLowerCase(), user.getDesc(), user == RequiredApiUser.ADMIN);
            }
        }
    }

    /**
     * Retrieves the ApiUser that matches the given apiKey.
     * @return ApiUser if found, null otherwise
     */
    public ApiUser getApiUser(String apiKey) {
        return apiUserDao.getApiUserByKey(apiKey);
    }

    /**
     * Adds a new Api user to the database.
     * @param name          Name of the user
     * @param description   Description of the user
     * @return ApiUser      If success returns a new ApiUser with id and apikey.
     *                      Upon failure, null is returned.
     */
    public ApiUser addApiUser(String name, String description, boolean admin) {
        // secure() backs the generator with SecureRandom, which makes output unpredictable to an attacker.
        var apiUser = new ApiUser(RandomStringUtils.secure().nextAlphanumeric(32), name, description, admin);
        if (apiUserDao.addApiUser(apiUser)) {
            return apiUserDao.getApiUserByKey(apiUser.getApiKey());
        }
        return null;
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
        if (StringUtils.isEmpty(key)) {
            // Grant access if user is in ip whitelist, or authenticated via the ui
            return !StringUtils.isEmpty(ipAddress) && ipAddress.matches(userIpFilter) &&
                    subject.hasRole("ADMIN");
        }

        // Return true if the user is already authenticated using the same key
        if (key.equals(subject.getPrincipal())) {
            return true;
        }

        // Validate the key and login if it is valid
        ApiUser potentialApiUser = getApiUser(key);
        if (potentialApiUser != null && potentialApiUser.isAdmin()) {
            subject.login(new ApiKeyLoginToken(key, ipAddress));
            ApiControllerUtil.setAuthenticated(request, true, potentialApiUser.getName());
            return true;
        }
        return false;
    }
}
