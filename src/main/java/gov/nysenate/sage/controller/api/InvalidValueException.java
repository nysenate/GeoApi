package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.model.result.ResultStatus;
import lombok.Getter;

@Getter
public class InvalidValueException extends IllegalArgumentException {
    private final ApiError apiError;

    public InvalidValueException(Class<?> caller) {
        this.apiError = new ApiError(caller, ResultStatus.VALUE_NOT_SUPPORTED);
    }

}
