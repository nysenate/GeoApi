package gov.nysenate.sage.model.result;

public class ApiErrorResult
{
    private String message;

    public ApiErrorResult(String message)
    {
        this.message = message;
    }

    public String getMessage()
    {
        return this.message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }
}
