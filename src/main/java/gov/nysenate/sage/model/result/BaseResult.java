package gov.nysenate.sage.model.result;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Serves as a base class to provide common fields to sub-classed results. Result objects are
 * typically returned by the service layer classes and are used to wrap a data model object with
 * some status information.
 */
public abstract class BaseResult {
    protected int serialId; // Can be used for maintaining order in a list
    protected List<String> messages = new ArrayList<>();
    protected ResultStatus statusCode = ResultStatus.SUCCESS;
    protected Class<?> source;
    protected Timestamp resultTime;

    public List<String> getMessages() {
        return messages;
    }

    public int getSerialId() {
        return serialId;
    }

    public void setSerialId(int serialId) {
        this.serialId = serialId;
    }

    public void addMessage(String message) {
        this.messages.add(message);
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    public Class getSource() {
        return source;
    }

    public void setSource(Class<?> sourceClass) {
        if (sourceClass != null) {
            this.source = sourceClass;
        }
    }

    public ResultStatus getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(ResultStatus statusCode) {
        this.statusCode = statusCode;
    }

    public boolean isSuccess() {
        return (this.statusCode != null && this.statusCode.equals(ResultStatus.SUCCESS));
    }

    public Timestamp getResultTime() {
        return resultTime;
    }

    public void setResultTime(Timestamp resultTime) {
        this.resultTime = resultTime;
    }
}
