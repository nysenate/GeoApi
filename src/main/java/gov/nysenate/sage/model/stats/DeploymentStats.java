package gov.nysenate.sage.model.stats;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

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

    private Deployment getLast(boolean isDeployed) {
        ListIterator<Deployment> deploymentIterator = deployments.listIterator(deployments.size() - 1);
        while (deploymentIterator.hasPrevious()) {
            Deployment deployment = deploymentIterator.previous();
            if (deployment.isDeployed() == isDeployed) {
                return deployment;
            }
        }
        return null;
    }

    /**
     * Returns the last (re)deployment info.
     * @return Deployment or null
     */
    public Deployment getLastDeployment() {
        return getLast(true);
    }

    /**
     * Returns the last undeployment info.
     * @return Deployment or null
     */
    public Deployment getLastUnDeployment() {
        return getLast(false);
    }

    public Timestamp getLastDeploymentTime() {
        Deployment deployment = getLastDeployment();
        return (deployment != null) ? deployment.getDeployTime() : null;
    }

    public long getLatestUptime()
    {
        Timestamp deployStart, deployEnd, current = new Timestamp(new Date().getTime());
        if (deployments.isEmpty()) return 0;

        int last = deployments.size() - 1;
        int secondLast = (last > 0) ? last - 1 : 0;
        if (deployments.get(last).isDeployed()) {
            deployStart = deployments.get(last).getDeployTime();
            deployEnd = current;
        }
        else {
            deployStart = deployments.get(secondLast).getDeployTime();
            deployEnd = deployments.get(last).getDeployTime();
        }

        return deployEnd.getTime() - deployStart.getTime();
    }
}