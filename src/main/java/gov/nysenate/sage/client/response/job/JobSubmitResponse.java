package gov.nysenate.sage.client.response.job;

public class JobSubmitResponse
{
    protected boolean success;
    protected String message;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
