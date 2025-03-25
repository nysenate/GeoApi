package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.model.result.ResultStatus;

public class InvalidValueException extends IllegalArgumentException {
    private final ApiError apiError;

    public InvalidValueException(Class<?> caller) {
        this.apiError = new ApiError(caller, ResultStatus.PROVIDER_NOT_SUPPORTED);
    }

    public ApiError getApiError() {
        return apiError;
    }
}
