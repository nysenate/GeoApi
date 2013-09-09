package gov.nysenate.sage.model.stats;

import gov.nysenate.sage.client.view.job.JobProcessStatusView;

import java.util.ArrayList;
import java.util.List;

public class JobUsageStats
{
    private List<JobProcessStatusView> jobProcesses;

    public JobUsageStats() {
        jobProcesses = new ArrayList<>();
    }

    public List<JobProcessStatusView> getJobProcesses() {
        return jobProcesses;
    }

    public void setJobProcesses(List<JobProcessStatusView> jobProcesses) {
        this.jobProcesses = jobProcesses;
    }
}