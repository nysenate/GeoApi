package gov.nysenate.sage.client.response.base;

import gov.nysenate.sage.model.result.BaseResult;
import gov.nysenate.sage.model.result.ResultStatus;

import java.util.LinkedHashSet;
import java.util.stream.Collectors;

public class BaseResponse {
    private final ResultStatus status;
    private LinkedHashSet<?> sources = null;

    public BaseResponse(BaseResult<?> baseResult) {
        if (baseResult != null) {
            this.status = baseResult.getStatusCode();
            this.sources = baseResult.getSources();
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

    public String getSources() {
        if (sources == null) {
            return null;
        }
        return sources.stream().map(Object::toString).collect(Collectors.joining(", "));
    }
}
