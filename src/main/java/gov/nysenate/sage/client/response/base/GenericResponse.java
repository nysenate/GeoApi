package gov.nysenate.sage.client.response.base;

public class GenericResponse {
    protected boolean success;
    protected String message;

    public GenericResponse(boolean success, String message) {
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
