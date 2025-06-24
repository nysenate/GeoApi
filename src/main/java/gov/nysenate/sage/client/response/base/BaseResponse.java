package gov.nysenate.sage.client.response.base;

import gov.nysenate.sage.model.result.BaseResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.provider.geocode.DataSource;

import java.util.ArrayList;
import java.util.List;

public class BaseResponse {
    private final ResultStatus status;
    private DataSource source = null;
    protected final List<String> messages = new ArrayList<>();

    public BaseResponse(BaseResult<?> baseResult) {
        if (baseResult != null) {
            this.status = baseResult.getStatusCode();
            this.source = baseResult.getSource();
            this.messages.addAll(baseResult.getMessages());
        }
        else {
            this.status = ResultStatus.RESPONSE_ERROR;
        }
    }

    public BaseResponse(ResultStatus status) {
        this.status = status;
    }

    public String getStatus() {
        return status.name();
    }

    public String getDescription() {
        return status.getDesc();
    }

    public int getStatusCode() {
        return status.getCode();
    }

    public String getSource() {
        return source == null ? null : source.toString();
    }

    public List<String> getMessages() {
        return this.messages;
    }
}
