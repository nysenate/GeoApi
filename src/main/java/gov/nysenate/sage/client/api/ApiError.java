package gov.nysenate.sage.client.api;

import gov.nysenate.sage.model.result.ResultStatus;

import java.util.ArrayList;
import java.util.List;

public class ApiError
{
    protected ResultStatus status;
    protected List<String> messages = new ArrayList<>();

    public ApiError(ResultStatus errorStatus)
    {
        this.status = errorStatus;
    }

    public String getStatus()
    {
        return status.name();
    }

    public String getDesc()
    {
        return status.getDesc();
    }

    public List<String> getMessages()
    {
        return this.messages;
    }
}
