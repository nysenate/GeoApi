package gov.nysenate.sage.client.response.base;

import gov.nysenate.sage.model.result.ResultStatus;

public class ApiError extends BaseResponse {
    private String className = "";

    public ApiError(ResultStatus resultStatus) {
        super(resultStatus);
    }

    public ApiError(Class<?> sourceClass, ResultStatus resultStatus) {
        super(resultStatus);
        this.className = sourceClass.getName();
    }

    public String getClassName() {
        return className;
    }
}
