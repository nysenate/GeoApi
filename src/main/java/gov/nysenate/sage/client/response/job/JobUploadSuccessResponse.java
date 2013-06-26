package gov.nysenate.sage.client.response.job;

import gov.nysenate.sage.model.job.JobProcess;

public class JobUploadSuccessResponse
{
    protected boolean success = true;
    protected JobProcess jobProcess;

    public JobUploadSuccessResponse(JobProcess jobProcess) {
        this.jobProcess = jobProcess;
    }

    public boolean isSuccess() {
        return success;
    }

    public JobProcess getJobProcess() {
        return jobProcess;
    }
}
