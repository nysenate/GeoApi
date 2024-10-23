package gov.nysenate.sage.service.security;

import gov.nysenate.sage.model.job.JobUser;
import gov.nysenate.sage.util.auth.JobUserAuth;
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
public class JobLoginAuthRealm extends SageAuthorizingRealm
{
    private static final Logger logger = LoggerFactory.getLogger(JobLoginAuthRealm.class);

    /** The IP whitelist is used here to restrict access to Job User login to internal IPs only. */
    @Value("${user.ip.filter}") private String ipWhitelist;

    private static class BCryptCredentialsMatcher implements CredentialsMatcher {

        /**
         * Compare a hashed password from the Auth token to the stored hash.
         * @param token The authentication credentials submitted by the user during a login attempt
         * @param info The valid authenticaton info to compare the token to
         * @return Whether or not the login credentials are valid
         */
        @Override
        public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
            UsernamePasswordToken userToken = (UsernamePasswordToken) token;
            String newPass = new String(userToken.getPassword());
            String registeredPass = (String) info.getCredentials();
            return newPass.equals(registeredPass);
        }
    }

    private static final BCryptCredentialsMatcher credentialsMatcher = new BCryptCredentialsMatcher();
    private final JobUserAuth jobUserAuth;

    @Autowired
    public JobLoginAuthRealm(JobUserAuth jobUserAuth) {
        this.jobUserAuth = jobUserAuth;
    }

    /**
     * This method will call the queryForAuthenticationInfo method in order to retrieve
     * authentication info about the given JobUser. If the query returns a valid job user account,
     * then this method will return an AuthenticationInfo for that job user account.
     *
     * @param token The given authentication information
     * @return Either valid AuthenticationInfo for the given token or null if the account is not valid
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        if (token instanceof UsernamePasswordToken usernamePasswordToken) {
            logger.info("Attempting login with Job Realm from IP {}", usernamePasswordToken.getHost());
            if (usernamePasswordToken.getHost().matches(ipWhitelist)) {
                return queryForAuthenticationInfo(usernamePasswordToken);
            }
            else {
                logger.warn("Blocking Job login from unauthorized IP {}", usernamePasswordToken.getHost());
                throw new AuthenticationException("Job User login from unauthorized IP address.");
            }
        }
        throw new UnsupportedTokenException(getName() + " only supports UsernamePasswordToken");
    }

    /**
     * This method uses the JobUser service to query the database and see if
     * the given username.
     * @param info The given UsernamePasswordToken
     * @return A new SimpleAuthenticationInfo object if the user is a valid job user, or AuthenticationException
     */
    protected AuthenticationInfo queryForAuthenticationInfo(UsernamePasswordToken info) {
        String username = info.getUsername();
        JobUser jobuser = jobUserAuth.getJobUser(username);
        return new SimpleAuthenticationInfo(jobuser.getEmail(), jobuser.getPassword(), getName());
    }

    /**
     * This method will return the Authorization Information for a particular job user
     * @param principals The identifying attributes of the currently active user
     * @return A SimpleAuthorizationInfo object containing the roles and permissions of the user.
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        Collection collection = principals.fromRealm(getName());
        if (!collection.isEmpty()) {
            SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
            String principal = collection.iterator().next().toString();
            logger.info("Determining job user roles for {}", principal);
            if (jobUserAuth.getJobUser(principal) != null) {
                info.addRole(SageRole.JOB_USER.name());
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