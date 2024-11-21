package gov.nysenate.sage.dao.logger.deployment;

public interface DeploymentLogger {
    /**
     * Logs deployment status to the database.
     */
    void logDeploymentStatus();
}
