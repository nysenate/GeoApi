package gov.nysenate.sage.client.response;

import gov.nysenate.sage.model.result.BaseResult;
import gov.nysenate.sage.model.result.ResultStatus;

import java.util.ArrayList;
import java.util.List;

public class BaseResponse
{
    protected ResultStatus status = ResultStatus.RESPONSE_ERROR;
    protected String source;
    protected List<String> messages = new ArrayList<>();

    public BaseResponse() {}

    public BaseResponse(BaseResult baseResult)
    {
        if (baseResult != null )
        {
            this.status = baseResult.getStatusCode();
            this.source = baseResult.getSource();
            this.messages = baseResult.getMessages();
        }
    }

    public BaseResponse(ResultStatus status)
    {
        this.status = status;
    }

    public BaseResponse(Class sourceClass, ResultStatus status)
    {
        this.source = sourceClass.getSimpleName();
        this.status = status;
    }

    public String getStatus()
    {
        return status.name();
    }

    public String getDescription()
    {
        return status.getDesc();
    }

    public int getStatusCode()
    {
        return status.getCode();
    }

    public String getSource() {
        return source;
    }

    public List<String> getMessages()
    {
        return this.messages;
    }
}
