package gov.nysenate.sage.client.response;

import gov.nysenate.sage.model.result.ResultStatus;

public class ApiError extends BaseResponse
{
    public ApiError(ResultStatus resultStatus) {
        super(resultStatus);
    }

    public ApiError(Class sourceClass, ResultStatus resultStatus) {
        super(sourceClass, resultStatus);
    }
}
