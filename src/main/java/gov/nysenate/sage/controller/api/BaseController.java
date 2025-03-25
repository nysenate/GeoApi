package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.base.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

public abstract class BaseController {
    @ExceptionHandler(InvalidValueException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ApiError handleInvalidValueException(InvalidValueException e) {
        return e.getApiError();
    }

    public <T extends Enum<T>> T getValue(String strValue, Class<T> enumClass) {
        try {
            return T.valueOf(enumClass, strValue.trim().toUpperCase());
        } catch (NullPointerException | IllegalArgumentException e) {
            throw new InvalidValueException(getClass());
        }
    }

    public <T extends Enum<T>> T getValueOrDefault(String strValue,  Class<T> enumClass, T defaultValue) {
        if (strValue == null || strValue.isBlank()) {
            return defaultValue;
        }
        return getValue(strValue, enumClass);
    }
}
