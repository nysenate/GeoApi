package gov.nysenate.sage.dao.model;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.job.JobUser;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.log4j.Logger;

import java.sql.SQLException;

/**
 * JobUserDao provides database persistence for the JobUser model.
 */
public class JobUserDao extends BaseDao
{
    private String SCHEMA = "job";
    private String TABLE = "user";
    private Logger logger = Logger.getLogger(JobUserDao.class);
    private ResultSetHandler<JobUser> handler = new BeanHandler<>(JobUser.class);
    private QueryRunner run = getQueryRunner();

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
            return run.update("INSERT INTO " + getTableName() + "(id,email,password,firstname,lastname,active) " +
                              "VALUES (?,?,?,?,?,?)",
                               jobUser.getId(), jobUser.getEmail(), jobUser.getPassword(), jobUser.getFirstname(),
                               jobUser.getLastname(), jobUser.isActive());
        }
        catch (SQLException sqlEx)
        {
            logger.error("Failed to add JobUser in JobUserDao!");
            logger.error(sqlEx.getMessage());
            return -1;
        }
    }

    private String getTableName()
    {
        return SCHEMA + "." + TABLE;
    }
}
