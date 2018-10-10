package gov.nysenate.sage.dao.model;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.job.JobUser;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.List;

/**
 * JobUserDao provides database persistence for the JobUser model.
 */
public class JobUserDao extends BaseDao
{
    private String SCHEMA = "job";
    private String TABLE = "user";
    private Logger logger = LogManager.getLogger(JobUserDao.class);
    private ResultSetHandler<JobUser> handler = new BeanHandler<>(JobUser.class);
    private ResultSetHandler<List<JobUser>> listHandler = new BeanListHandler<>(JobUser.class);
    private QueryRunner run = getQueryRunner();

    public List<JobUser> getJobUsers()
    {
        try {
            return run.query("SELECT * FROM " + getTableName(), listHandler);
        }
        catch (SQLException sqlEx) {
            logger.error("Failed to get JobUsers!");
            logger.error(sqlEx.getMessage());
        }
        return null;
    }

    public JobUser getJobUserById(int id)
    {
        try {
            return run.query("SELECT * FROM " + getTableName() + " WHERE id = ?", handler, id);
        }
        catch (SQLException sqlEx) {
            logger.error("Failed to get JobUser by email in JobUserDao!");
            logger.error(sqlEx.getMessage());
        }
        return null;
    }

    /**
     * Retrieves an JobUser from the database by email.
     * @param email
     * @return JobUser   The matched JobUser or null if not found.
     */
    public JobUser getJobUserByEmail(String email)
    {
        try {
            return run.query("SELECT * FROM " + getTableName() + " WHERE email = ?", handler, email);
        }
        catch (SQLException sqlEx) {
            logger.error("Failed to get JobUser by email in JobUserDao!");
            logger.error(sqlEx.getMessage());
        }
        return null;
    }

    /**
     * Adds a JobUser to the database.
     * @param jobUser   The JobUser to add.
     * @return int      1 if user was inserted, 0 otherwise.
     */
    public int addJobUser(JobUser jobUser)
    {
        try {
            return run.update("INSERT INTO " + getTableName() + "(email,password,firstname,lastname,active,admin) " +
                              "VALUES (?,?,?,?,?,?)",
                               jobUser.getEmail(), jobUser.getPassword(), jobUser.getFirstname(),
                               jobUser.getLastname(), jobUser.isActive(), jobUser.isAdmin());
        }
        catch (SQLException sqlEx) {
            logger.error("Failed to add JobUser in JobUserDao!");
            logger.error(sqlEx.getMessage());
            return -1;
        }
    }

    /**
     * Removes a JobUser from the database.
     * @param jobUser
     * @return  1 if user was removed, 0 otherwise.
     */
    public int removeJobUser(JobUser jobUser)
    {
        try {
            return run.update("DELETE FROM " + getTableName() + " WHERE id = ?", jobUser.getId());
        }
        catch (SQLException sqlEx) {
            logger.error("Failed to remove JobUser in JobUserDao!");
            logger.error(sqlEx.getMessage());
        }
        return 0;
    }

    private String getTableName()
    {
        return SCHEMA + "." + TABLE;
    }
}
