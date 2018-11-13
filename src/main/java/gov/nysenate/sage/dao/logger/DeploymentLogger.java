package gov.nysenate.sage.dao.logger;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.base.ReturnIdHandler;
import org.apache.commons.dbutils.QueryRunner;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.sql.Timestamp;

@Repository
public class DeploymentLogger extends BaseDao
{
    private static Logger logger = LoggerFactory.getLogger(DeploymentLogger.class);
    private static String SCHEMA = "log";
    private static String TABLE = "deployment";
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
        return 0;
    }
}
