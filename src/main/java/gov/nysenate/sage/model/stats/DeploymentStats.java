package gov.nysenate.sage.model.stats;

import java.sql.Timestamp;
import java.util.List;

public class DeploymentStats
{
    private List<Deployment> deployments;

    public DeploymentStats(List<Deployment> deployments)
    {
        this.deployments = deployments;
    }

    public List<Deployment> getDeployments() {
        return deployments;
    }

    public void setDeployments(List<Deployment> deployments) {
        this.deployments = deployments;
    }

    public int getLatestUptime()
    {
        return 0;
    }

    public int getLatestDowntime()
    {
        return 0;
    }

    public int getAverageUptime()
    {
        return 0;
    }
}