package gov.nysenate.sage.service.security;

import gov.nysenate.sage.model.api.ApiUser;
import gov.nysenate.sage.util.auth.ApiUserAuth;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authc.pam.UnsupportedTokenException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class ApiUserAuthRealm extends SageAuthorizingRealm
{
    private static final Logger logger = LoggerFactory.getLogger(ApiUserAuthRealm.class);

    /** The IP whitelist is used here to restrict access to Api User login to internal IPs only. */
    @Value("${user.ip.filter}") private String ipWhitelist;

    private static class ApiCredentialsMatcher implements CredentialsMatcher {

        /**
         * Compare a hashed password from the Auth token to the stored hash.
         * @param token The authentication credentials submitted by the user during a login attempt
         * @param info The valid authenticaton info to compare the token to
         * @return Whether or not the login credentials are valid
         */
        @Override
        public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
            if (token != null && info != null && token.getPrincipal() != null && info.getPrincipals() != null) {
                return StringUtils.equals(token.getPrincipal().toString(),
                        info.getPrincipals().getPrimaryPrincipal().toString());
            }
            return false;
        }
    }

    private static final CredentialsMatcher apiCredentialsMatcher = new ApiCredentialsMatcher();


    private ApiUserAuth apiUserAuth;

    @Autowired
    public ApiUserAuthRealm(ApiUserAuth apiUserAuth) {
        this.apiUserAuth = apiUserAuth;
    }

    /**
     * This method will call the queryForAuthenticationInfo method in order to retrieve
     * authentication info about the given ApiUser. If the query returns a valid api user account,
     * then this method will return an AuthenticationInfo for that api user account.
     *
     * @param token The given authentication information
     * @return Either valid AuthenticationInfo for the given token or null if the account is not valid
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        if (token != null && token instanceof ApiKeyLoginToken) {
            ApiKeyLoginToken apiKeyLoginToken = (ApiKeyLoginToken) token;
            logger.info("Attempting login with Api User Realm from IP {}", apiKeyLoginToken.getHost());
            if (apiKeyLoginToken.getHost().matches(ipWhitelist)) {
                return queryForAuthenticationInfo(apiKeyLoginToken);
            }
            else {
                logger.warn("Blocking api user login from unauthorized IP {}", apiKeyLoginToken.getHost());
                throw new AuthenticationException("Api user login from unauthorized IP address.");
            }
        }
        throw new UnsupportedTokenException(getName() + " only supports ApiKeyLoginToken");
    }

    /**
     * This method uses the ApiUser service to query the database and see if
     * the given username.
     * @param info The given UsernamePasswordToken
     * @return A new SimpleAuthenticationInfo object if the user is a valid api user, or AuthenticationException
     */
    protected AuthenticationInfo queryForAuthenticationInfo(ApiKeyLoginToken info) {
        String apiKey = info.getApiKey();
        ApiUser apiUser = apiUserAuth.getApiUser(apiKey);
        return new SimpleAuthenticationInfo(apiUser.getApiKey(), apiUser.getId(), getName());
    }

    /**
     * This method will return the Authorization Information for a particular api user
     * @param principals The identifying attributes of the currently active user
     * @return A SimpleAuthorizationInfo object containing the roles and permissions of the user.
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        Collection collection = principals.fromRealm(getName());
        if (!collection.isEmpty()) {
            SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
            String principal = collection.iterator().next().toString();
            logger.info("Determining api user roles for {}", principal);
            if (apiUserAuth.getApiUser(principal) != null) {
                info.addRole(SageRole.API_USER.name());
            }
            return info;
        }
        return null;
    }

    /**
     * Use the BCrypt credentials matcher.
     * @return The BCrypt credentials matcher.
     */
    @Override
    public CredentialsMatcher getCredentialsMatcher() {
        return apiCredentialsMatcher;
    }

    @Override
    public Class getAuthenticationTokenClass() {
        return ApiKeyLoginToken.class;
    }
}