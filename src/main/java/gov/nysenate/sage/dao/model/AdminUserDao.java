package gov.nysenate.sage.dao.model;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.admin.AdminUser;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.List;

/**
 * AdminUserDao provides database persistence for the AdminUser model.
 */
public class AdminUserDao extends BaseDao
{
    private Logger logger = Logger.getLogger(AdminUserDao.class);
    private ResultSetHandler<AdminUser> handler = new BeanHandler<>(AdminUser.class);
    private QueryRunner run = getQueryRunner();

    private static String SCHEMA = "public";
    private static String TABLE = "admin";

    /**
     * Check if the admin user credentials are valid.
     * @param username  Admin username
     * @param password  Admin password
     * @return true if valid credentials, false otherwise.
     */
    public boolean checkAdminUser(String username, String password)
    {
        AdminUser adminUser = null;
        String sql = "SELECT * FROM " + SCHEMA + "." + TABLE + "\n" +
                     "WHERE username = ?";
        try {
            adminUser = run.query(sql, handler, username);
        }
        catch (SQLException ex) {
            logger.error("Failed to retrieve admin user!", ex);
        }

        if (adminUser != null) {
            return BCrypt.checkpw(password, adminUser.getPassword());
        }
        return false;
    }
}
