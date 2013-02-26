package gov.nysenate.sage.model.result;

import java.util.ArrayList;

/**
 * Serves as a base class to provide common fields to sub-classed results.
 */
public abstract class BaseResult
{
    protected ArrayList<String> messages = new ArrayList<>();
    protected ResultStatus statusCode = ResultStatus.NO_ERROR;
    protected String status = "";
    protected String source = "";

    public ArrayList<String> getMessages()
    {
        return messages;
    }

    public void addMessage(String message)
    {
        this.messages.add(message);
    }

    public void setMessages(ArrayList<String> messages)
    {
        this.messages = messages;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        if (status != null){
            this.status = status;
        }
    }

    public String getSource()
    {
        return source;
    }

    public void setSource(Class sourceClass)
    {
        if (sourceClass != null){
            this.source = sourceClass.getSimpleName();
        }
    }

    public ResultStatus getStatusCode()
    {
        return statusCode;
    }

    public void setStatusCode(ResultStatus statusCode)
    {
        this.statusCode = statusCode;
    }
}
