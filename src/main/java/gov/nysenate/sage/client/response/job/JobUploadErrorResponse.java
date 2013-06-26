package gov.nysenate.sage.client.response.job;

public class JobUploadErrorResponse
{
    protected String error;

    public JobUploadErrorResponse(String errorMessage) {
        this.error = errorMessage;
    }

    public String getError() {
        return error;
    }
}
