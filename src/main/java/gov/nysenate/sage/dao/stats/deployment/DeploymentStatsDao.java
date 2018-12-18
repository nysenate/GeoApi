package gov.nysenate.sage.dao.stats.deployment;

import gov.nysenate.sage.model.stats.DeploymentStats;

import java.sql.Timestamp;

public interface DeploymentStatsDao {
    /**
     * Get DeploymentStats from the database
     * @return DeploymentStats
     */
    public DeploymentStats getDeploymentStats();

    /**
     * Get deployment stats for a given time range.
     * @param since Timestamp for the beginning of the range.
     * @param until Timestamp for the end of the range.
     * @return DeploymentStats
     */
    public DeploymentStats getDeploymentStatsDuring(Timestamp since, Timestamp until);
}
