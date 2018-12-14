package gov.nysenate.sage.dao.logger.deployment;

import java.sql.Timestamp;

public interface DeploymentLogger {

    /**
     * Logs deployment status to the database.
     * @param deployed Set to true to indicate deployment, false for un-deployment.
     * @param deploymentId If un-deploying, set to the id obtained when previously deployed.
     *                     If deploying just set this to -1.
     * @param deployTime Timestamp
     * @return deployment Id
     */
    public Integer logDeploymentStatus(boolean deployed, Integer deploymentId, Timestamp deployTime);


}
