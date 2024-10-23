package gov.nysenate.sage.model.stats;

import java.util.List;

public class DeploymentStats {
    private List<Deployment> deployments;

    public DeploymentStats(List<Deployment> deployments) {
        this.deployments = deployments;
    }

    public List<Deployment> getDeployments() {
        return deployments;
    }

    public void setDeployments(List<Deployment> deployments) {
        this.deployments = deployments;
    }
}