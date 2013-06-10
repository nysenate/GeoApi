package gov.nysenate.sage.model.result;

import java.util.ArrayList;

/**
 * Serves as a base class to provide common fields to sub-classed results. Result objects are
 * typically returned by the service layer classes and are used to wrap a data model object with
 * some status information.
 */
public abstract class BaseResult
{
    protected int serialId; // Can be used for maintaining order in a list
    protected ArrayList<String> messages = new ArrayList<>();
    protected ResultStatus statusCode = ResultStatus.SUCCESS;
    protected String source = "";

    public ArrayList<String> getMessages()
    {
        return messages;
    }

    public int getSerialId() {
        return serialId;
    }

    public void setSerialId(int serialId) {
        this.serialId = serialId;
    }

    public void addMessage(String message)
    {
        this.messages.add(message);
    }

    public void setMessages(ArrayList<String> messages)
    {
        this.messages = messages;
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

    public boolean isSuccess() {
        return (this.statusCode != null && this.statusCode.equals(ResultStatus.SUCCESS));
    }
}
