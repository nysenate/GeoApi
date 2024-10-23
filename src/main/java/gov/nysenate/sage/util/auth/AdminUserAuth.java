package gov.nysenate.sage.util.auth;

import gov.nysenate.sage.dao.model.admin.SqlAdminUserDao;
import gov.nysenate.sage.model.admin.AdminUser;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

import static gov.nysenate.sage.util.controller.ApiControllerUtil.setAuthenticated;

@Component
public class AdminUserAuth {
    private static final Logger logger = LoggerFactory.getLogger(AdminUserAuth.class);
    private final SqlAdminUserDao sqlAdminUserDao;

    @Autowired
    public AdminUserAuth(SqlAdminUserDao sqlAdminUserDao) {
        this.sqlAdminUserDao = sqlAdminUserDao;
    }

    public AdminUser getAdminUser(String username) {
        return sqlAdminUserDao.getAdminUser(username);
    }

    public boolean checkAdminUser(String username, String password) {
        return sqlAdminUserDao.checkAdminUser(username, password);
    }

    public AdminUser insertAdminUser(String username, String password) {
        sqlAdminUserDao.insertAdmin(username, BCrypt.hashpw(password, BCrypt.gensalt()));
        return sqlAdminUserDao.getAdminUser(username);
    }

    public boolean authenticateAdmin(HttpServletRequest request, String username, String password,
                                     Subject subject, String ipAddr) {
        boolean validInput = !username.equals("defaultUser") && !password.equals("defaultPass")
                && sqlAdminUserDao.checkAdminUser(username, password);

        if (validInput) {
            logger.debug("Granted admin access to {}", username);
            AdminUser dbAdmin = sqlAdminUserDao.getAdminUser(username);
            subject.login(new UsernamePasswordToken(username, dbAdmin.getPassword() , ipAddr));
            setAuthenticated(request, true, username);
            return true;
        }
        return false;
    }
}
