package gov.nysenate.sage.model.result;

public class JobErrorResult
{
    protected boolean success = false;
    protected String message;

    public JobErrorResult(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
