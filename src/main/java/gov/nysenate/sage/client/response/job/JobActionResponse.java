package gov.nysenate.sage.client.response.job;

public class JobActionResponse
{
    protected boolean success;
    protected String message;

    public JobActionResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
