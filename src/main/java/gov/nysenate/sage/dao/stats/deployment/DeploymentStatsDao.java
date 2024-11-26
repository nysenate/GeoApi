package gov.nysenate.sage.dao.stats.deployment;

import gov.nysenate.sage.model.stats.Deployment;

import java.util.List;

public interface DeploymentStatsDao {
    /**
     * Get DeploymentStats from the database
     * @return DeploymentStats
     */
    List<Deployment> getDeploymentStats();
}
