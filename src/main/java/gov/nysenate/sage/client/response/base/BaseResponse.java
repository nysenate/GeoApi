package gov.nysenate.sage.client.response.base;

import gov.nysenate.sage.model.result.ResultStatus;

public class BaseResponse {
    private final ResultStatus status;

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
}
