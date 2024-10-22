package gov.nysenate.sage.dao.stats.deployment;

import gov.nysenate.sage.model.stats.DeploymentStats;

public interface DeploymentStatsDao {
    /**
     * Get DeploymentStats from the database
     * @return DeploymentStats
     */
    DeploymentStats getDeploymentStats();
}
