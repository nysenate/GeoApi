package gov.nysenate.sage.dao.logger;

import com.sun.org.apache.bcel.internal.generic.RETURN;
import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.base.ReturnIdHandler;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.sql.Timestamp;

public class DeploymentLogger extends BaseDao
{
    private static Logger logger = Logger.getLogger(DeploymentLogger.class);
    private static String SCHEMA = "log";
    private static String TABLE = "deployments";
    private QueryRunner run = getQueryRunner();

    /**
     * Logs deployment status to the database.
     * @param deployed Set to true to indicate deployment, false for un-deployment.
     * @param deploymentId If un-deploying, set to the id obtained when previously deployed.
     *                     If deploying just set this to -1.
     * @param deployTime Timestamp
     * @return deployment Id
     */
    public Integer logDeploymentStatus(boolean deployed, Integer deploymentId, Timestamp deployTime)
    {
        String sql = "INSERT INTO " + SCHEMA + "." + TABLE + "(deployed, refId, deployTime) \n" +
                     "VALUES (?, ?, ?) \n" +
                     "RETURNING id";
        try {
            return run.query(sql, new ReturnIdHandler(), deployed, deploymentId, deployTime);
        }
        catch (SQLException ex) {
            logger.error("Failed to log deployment");
        }
        return -1;
    }
}
