package gov.nysenate.sage.dao.model.admin;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.admin.AdminUser;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
public class SqlAdminUserDao implements AdminUserDao
{
    private Logger logger = LoggerFactory.getLogger(SqlAdminUserDao.class);
    private BaseDao baseDao;

    @Autowired
    public SqlAdminUserDao(BaseDao baseDao) {
        this.baseDao = baseDao;
    }

    /** {@inheritDoc} */
    public boolean checkAdminUser(String username, String password)
    {
        AdminUser adminUser = null;
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("username", username);

            adminUser = getAdminUser(username);
        }
        catch (Exception ex) {
            logger.error("Failed to retrieve admin user!", ex);
        }

        if (adminUser != null) {
            return BCrypt.checkpw(password, adminUser.getPassword());
        }
        return false;
    }

    /** {@inheritDoc} */
    public AdminUser getAdminUser(String username) {
        AdminUser adminUser = null;
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("username", username);
            List<AdminUser> adminUserList = baseDao.geoApiNamedJbdcTemplate.query(
                    AdminUserQuery.GET_ADMIN.getSql(baseDao.getPublicSchema()), params, new AdminUserHandler() );
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
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("username", username);
            params.addValue("password", password);
            baseDao.geoApiNamedJbdcTemplate.update(AdminUserQuery.INSERT_ADMIN.getSql(baseDao.getPublicSchema()), params);
        }
        catch (Exception e) {
            logger.error("Failed to insert admin user!", e);
        }

    }

    private static class AdminUserHandler implements RowMapper<AdminUser> {
        public AdminUser mapRow(ResultSet rs, int rowNum) throws SQLException {
            AdminUser adminUser = new AdminUser();
            adminUser.setId( rs.getInt("id") );
            adminUser.setUsername( rs.getString("username") );
            adminUser.setPassword( rs.getString("password") );
            return adminUser;
        }
    }
}
