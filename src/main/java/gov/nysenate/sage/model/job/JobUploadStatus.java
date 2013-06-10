package gov.nysenate.sage.model.job;

public class JobUploadStatus
{
    protected boolean success;
    protected JobProcess process;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public JobProcess getProcess() {
        return process;
    }

    public void setProcess(JobProcess process) {
        this.process = process;
    }
}
