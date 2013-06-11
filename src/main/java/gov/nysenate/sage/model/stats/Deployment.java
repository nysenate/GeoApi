package gov.nysenate.sage.model.stats;

import java.sql.Timestamp;

public class Deployment
{
    private int id;
    private boolean deployed;
    private int deploymentRef;
    private Timestamp deployTime;
    private int apiRequestsSince;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isDeployed() {
        return deployed;
    }

    public void setDeployed(boolean deployed) {
        this.deployed = deployed;
    }

    public int getDeploymentRef() {
        return deploymentRef;
    }

    public void setDeploymentRef(int deploymentRef) {
        this.deploymentRef = deploymentRef;
    }

    public Timestamp getDeployTime() {
        return deployTime;
    }

    public void setDeployTime(Timestamp deployTime) {
        this.deployTime = deployTime;
    }

    public int getApiRequestsSince() {
        return apiRequestsSince;
    }

    public void setApiRequestsSince(int apiRequestsSince) {
        this.apiRequestsSince = apiRequestsSince;
    }
}
