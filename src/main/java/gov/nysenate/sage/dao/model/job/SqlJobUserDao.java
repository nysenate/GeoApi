package gov.nysenate.sage.dao.model.job;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.job.JobUser;
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
 * JobUserDao provides database persistence for the JobUser model.
 */
@Repository
public class SqlJobUserDao {
    private static final Logger logger = LoggerFactory.getLogger(SqlJobUserDao.class);
    private final BaseDao baseDao;

    @Autowired
    public SqlJobUserDao(BaseDao baseDao) {
        this.baseDao = baseDao;
    }

    public List<JobUser> getJobUsers() {
        try {
            return baseDao.geoApiNamedJbdcTemplate.query(
                    JobUserQuery.GET_ALL_JOB_USERS.getSql(baseDao.getJobSchema()), new JobUserHandler());
        }
        catch (Exception sqlEx) {
            logger.error("Failed to get JobUsers!");
            logger.error(sqlEx.getMessage());
        }
        return null;
    }

    public JobUser getJobUserById(int id) {
        try {
            if (id == 1) {
                return null;
            }

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("id", id);

            List<JobUser> jobUserList = baseDao.geoApiNamedJbdcTemplate.query(
                    JobUserQuery.GET_JOB_USER_BY_ID.getSql(baseDao.getJobSchema()),
                    params, new JobUserHandler());

            if (!jobUserList.isEmpty() && jobUserList.get(0) != null) {
                return jobUserList.get(0);
            }
        }
        catch (Exception sqlEx) {
            logger.error("Failed to get JobUser by id in JobUserDao with id of {}", id);
            logger.error(sqlEx.getMessage());
        }
        return null;
    }

    public JobUser getJobUserByEmail(String email) {
        try {
            var params = new MapSqlParameterSource("email", email);
            List<JobUser> jobUserList = baseDao.geoApiNamedJbdcTemplate.query(
                    JobUserQuery.GET_JOB_USER_BY_EMAIL.getSql(baseDao.getJobSchema()),
                    params, new JobUserHandler());

            if (jobUserList.get(0) != null) {
                return jobUserList.get(0);
            }
        }
        catch (Exception sqlEx) {
            logger.error("Failed to get JobUser by email in JobUserDao!");
            logger.error(sqlEx.getMessage());
        }
        return null;
    }

    /**
     * Add a job user who can use SAGE batch services
     */
    public int addJobUser(JobUser jobUser) {
        try {
            var params = new MapSqlParameterSource()
                    .addValue("email",jobUser.getEmail())
                    .addValue("password",jobUser.getPassword())
                    .addValue("firstname",jobUser.getFirstname())
                    .addValue("lastname",jobUser.getLastname())
                    .addValue("active",jobUser.isActive())
                    .addValue("admin",jobUser.isAdmin());

            return baseDao.geoApiNamedJbdcTemplate.update(JobUserQuery.INSERT_JOB_USER.getSql(baseDao.getJobSchema()),
                    params);
        }
        catch (Exception sqlEx) {
            logger.error("Failed to add JobUser in JobUserDao!");
            logger.error(sqlEx.getMessage());
            return -1;
        }
    }

    public int removeJobUser(JobUser jobUser) {
        try {
            var params = new MapSqlParameterSource("id", jobUser.getId());
            return baseDao.geoApiNamedJbdcTemplate.update(JobUserQuery.REMOVE_JOB_USER.getSql(baseDao.getJobSchema()), params);
        }
        catch (Exception sqlEx) {
            logger.error("Failed to remove JobUser in JobUserDao!");
            logger.error(sqlEx.getMessage());
        }
        return 0;
    }

    private static class JobUserHandler implements RowMapper<JobUser> {
        @Override
        public JobUser mapRow(ResultSet rs, int rowNum) throws SQLException {
            var jobUser = new JobUser();
            jobUser.setId(rs.getInt("id"));
            jobUser.setEmail(rs.getString("email"));
            jobUser.setPassword(rs.getString("password"));
            jobUser.setFirstname(rs.getString("firstname"));
            jobUser.setLastname(rs.getString("lastname"));
            jobUser.setActive(rs.getBoolean("active"));
            jobUser.setAdmin(rs.getBoolean("admin"));
            return jobUser;
        }
    }
}
