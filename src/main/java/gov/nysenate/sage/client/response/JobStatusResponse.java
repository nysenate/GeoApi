package gov.nysenate.sage.client.response;

import gov.nysenate.sage.client.view.JobProcessStatusView;
import gov.nysenate.sage.model.job.JobProcessStatus;
import gov.nysenate.sage.model.result.JobErrorResult;

import java.util.ArrayList;
import java.util.List;

public class JobStatusResponse
{
    protected boolean success = false;
    protected List<JobProcessStatusView> statuses = new ArrayList<>();
    protected String message = "";

    public JobStatusResponse(JobProcessStatus jobProcessStatus)
    {
        if (jobProcessStatus != null) {
            this.success = true;
            this.statuses.add(new JobProcessStatusView(jobProcessStatus));
        }
    }

    public JobStatusResponse(List<JobProcessStatus> jobProcessStatuses)
    {
        if (jobProcessStatuses != null) {
            this.success = true;
            for (JobProcessStatus jps: jobProcessStatuses) {
                this.statuses.add(new JobProcessStatusView(jps));
            }
        }
    }

    public JobStatusResponse(JobErrorResult jobErrorResult)
    {
        this.success = false;
        this.message = jobErrorResult.getMessage();
    }

    public List<JobProcessStatusView> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<JobProcessStatusView> statuses) {
        this.statuses = statuses;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
