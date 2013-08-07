package gov.nysenate.sage.client.response.base;

import gov.nysenate.sage.model.result.ResultStatus;

public class ApiError extends BaseResponse
{
    public ApiError(ResultStatus resultStatus) {
        super(resultStatus);
        this.source = "Api Filter";
    }

    public ApiError(Class sourceClass, ResultStatus resultStatus) {
        super(sourceClass, resultStatus);
        this.source = "Api Filter";
    }
}
