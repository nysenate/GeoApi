package gov.nysenate.sage.model.result;

import gov.nysenate.sage.provider.geocode.DataSource;

import java.sql.Timestamp;
import java.util.*;

/**
 * Serves as a base class to provide common fields to sub-classed results. Result objects are
 * typically returned by the service layer classes and are used to wrap a data model object with
 * some status information.
 */
public abstract class BaseResult<S extends DataSource> {
    // A null source means the service was never actually hit for a result.
    private final LinkedHashSet<S> sources;
    protected int serialId; // Can be used for maintaining order in a list
    protected List<String> messages = new ArrayList<>();
    protected ResultStatus statusCode = ResultStatus.SUCCESS;
    protected Timestamp resultTime;

    protected BaseResult(S source) {
        this.sources = source == null ? null : new LinkedHashSet<>(Set.of(source));
    }

    protected BaseResult(LinkedHashSet<S> sources) {
        this.sources = sources;
    }

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

    public LinkedHashSet<S> getSources() {
        return sources;
    }

    public ResultStatus getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(ResultStatus statusCode) {
        this.statusCode = statusCode;
    }

    // TODO: feel like this should be abstract, based on other data members
    public boolean isSuccess() {
        return statusCode == ResultStatus.SUCCESS;
    }

    public Timestamp getResultTime() {
        return resultTime;
    }

    public void setResultTime() {
        this.resultTime = new Timestamp(new Date().getTime());
    }
}
