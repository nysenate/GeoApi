package gov.nysenate.sage.service.security;

import gov.nysenate.sage.model.admin.AdminUser;
import gov.nysenate.sage.util.auth.AdminUserAuth;
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

import javax.annotation.PostConstruct;
import java.util.Collection;

@Component
public class AdminLoginAuthRealm extends SageAuthorizingRealm {
    private static final Logger logger = LoggerFactory.getLogger(AdminLoginAuthRealm.class);
    private static final BCryptCredentialsMatcher credentialsMatcher = new BCryptCredentialsMatcher();
    private final AdminUserAuth adminUserAuth;
    @Value("${default.admin.username}")
    private String defaultAdminName;
    @Value("${default.admin.password}")
    private String defaultAdminPass;

    /** The IP whitelist is used here to restrict access to admin login to internal IPs only. */
    @Value("${user.ip.filter}") private String ipWhitelist;

    private static class BCryptCredentialsMatcher implements CredentialsMatcher {
        /**
         * Compare a hashed password from the Auth token to the stored hash.
         * @param token The authentication credentials submitted by the user during a login attempt
         * @param info The valid authenticaton info to compare the token to
         * @return Whether the login credentials are valid
         */
        @Override
        public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
            UsernamePasswordToken userToken = (UsernamePasswordToken) token;
            String newPass = new String(userToken.getPassword());
            String registeredPass = (String) info.getCredentials();
            return newPass.equals(registeredPass);
        }
    }

    @Autowired
    public AdminLoginAuthRealm(AdminUserAuth adminUserAuth) {
        this.adminUserAuth = adminUserAuth;
    }

    @PostConstruct
    public void setup() throws RuntimeException {
        if (!adminUserAuth.checkAdminUser(defaultAdminName, defaultAdminPass)) {
            logger.info("Default admin user not present in database, creating default admin");
            adminUserAuth.insertAdminUser(defaultAdminName, defaultAdminPass);
        }
    }

    /**
     * This method will call the queryForAuthenticationInfo method in order to retrieve
     * authentication info about the given admin. If the query returns a valid admin account,
     * then this method will return an AuthenticationInfo for that admin account.
     *
     * @param token The given authentication information
     * @return Either valid AuthenticationInfo for the given token or null if the account is not valid
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        if (token instanceof UsernamePasswordToken usernamePasswordToken) {
            logger.info("Attempting login with Admin Realm from IP {}", usernamePasswordToken.host());
            if (usernamePasswordToken.host().matches(ipWhitelist)) {
                return queryForAuthenticationInfo(usernamePasswordToken);
            }
            else {
                logger.warn("Blocking admin login from unauthorized IP {}", usernamePasswordToken.host());
                throw new AuthenticationException("Admin login from unauthorized IP address.");
            }
        }
        throw new UnsupportedTokenException(getName() + " only supports UsernamePasswordToken");
    }

    /**
     * This method uses the AdminUser service to query the database and see if
     * the given username.
     * @param info The given UsernamePasswordToken
     * @return A new SimpleAuthenticationInfo object if the user is a valid Admin, or AuthenticationException
     */
    protected AuthenticationInfo queryForAuthenticationInfo(UsernamePasswordToken info) {
        String username = info.getUsername();
        AdminUser admin = adminUserAuth.getAdminUser(username);
        return new SimpleAuthenticationInfo(admin.getUsername(), admin.getPassword(), getName());
    }

    /**
     * This method will return the Authorization Information for a particular admin
     * @param principals The identifying attributes of the currently active user
     * @return A SimpleAuthorizationInfo object containing the roles and permissions of the user.
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        Collection collection = principals.fromRealm(getName());
        if (!collection.isEmpty()) {
            SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
            String principal = collection.iterator().next().toString();
            logger.info("Determining admin roles for {}", principal);
            if (adminUserAuth.getAdminUser(principal) != null) {
                info.addRole(SageRole.ADMIN.name());
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
        return credentialsMatcher;
    }

    @Override
    public Class<UsernamePasswordToken> getAuthenticationTokenClass() {
        return UsernamePasswordToken.class;
    }
}
