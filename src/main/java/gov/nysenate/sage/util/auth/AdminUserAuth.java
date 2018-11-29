package gov.nysenate.sage.util.auth;

import gov.nysenate.sage.dao.model.SqlAdminUserDao;
import gov.nysenate.sage.model.admin.AdminUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AdminUserAuth {

    private Logger logger = LoggerFactory.getLogger(JobUserAuth.class);
    private SqlAdminUserDao sqlAdminUserDao;

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
}
