package gov.nysenate.sage.dao.model.admin;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.admin.AdminUser;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * AdminUserDao provides database persistence for the AdminUser model.
 */
@Repository
public class SqlAdminUserDao extends BaseDao implements AdminUserDao {
    private static final Logger logger = LoggerFactory.getLogger(SqlAdminUserDao.class);

    /** {@inheritDoc} */
    public boolean checkAdminUser(String username, String password) {
        try {
            AdminUser adminUser = getAdminUser(username);
            return BCrypt.checkpw(password, adminUser.getPassword());
        }
        catch (Exception ex) {
            logger.error("Failed to retrieve admin user!", ex);
            return false;
        }
    }

    /** {@inheritDoc} */
    public AdminUser getAdminUser(String username) {
        AdminUser adminUser = null;
        try {
            var params = new MapSqlParameterSource("username", username);
            List<AdminUser> adminUserList = geoApiNamedJbdcTemplate.query(
                    AdminUserQuery.GET_ADMIN.getSql(getPublicSchema()), params, new AdminUserHandler());
            if (!adminUserList.isEmpty() && adminUserList.get(0) != null) {
                adminUser = adminUserList.get(0);
            }
        }
        catch (Exception ex) {
            if (username.contains("@")) {
                logger.debug("Job Users cant be validated in the Admin Dao");
            }
            else {
                logger.error("Failed to retrieve admin user!", ex);
            }
        }
        return adminUser;
    }

    /** {@inheritDoc} */
    public void insertAdmin(String username, String password) {
        try {
            var params = new MapSqlParameterSource("username", username)
                    .addValue("password", password);
            geoApiNamedJbdcTemplate.update(AdminUserQuery.INSERT_ADMIN.getSql(getPublicSchema()), params);
        }
        catch (Exception e) {
            logger.error("Failed to insert admin user!", e);
        }

    }

    private static class AdminUserHandler implements RowMapper<AdminUser> {
        public AdminUser mapRow(ResultSet rs, int rowNum) throws SQLException {
            var adminUser = new AdminUser();
            adminUser.setId( rs.getInt("id") );
            adminUser.setUsername(rs.getString("username"));
            adminUser.setPassword(rs.getString("password"));
            return adminUser;
        }
    }
}
