package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.model.district.NoShapefileForDistrictTypeException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import static gov.nysenate.sage.model.result.ResultStatus.UNSUPPORTED_DISTRICT_MAP;

public abstract class BaseController {
    @ExceptionHandler(InvalidValueException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ApiError handleInvalidValueException(InvalidValueException e) {
        return e.getApiError();
    }

    @ExceptionHandler(NoShapefileForDistrictTypeException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ApiError handleNoShapefileForDistrictTypeException(NoShapefileForDistrictTypeException e) {
        return new ApiError(UNSUPPORTED_DISTRICT_MAP);
    }

    public <T extends Enum<T>> T getValue(String strValue, Class<T> enumClass) {
        try {
            return T.valueOf(enumClass, strValue.trim().toUpperCase());
        } catch (NullPointerException | IllegalArgumentException e) {
            throw new InvalidValueException(getClass());
        }
    }

    public <T extends Enum<T>> T getValueOrNull(String strValue, Class<T> enumClass) {
        if (strValue == null || strValue.isBlank()) {
            return null;
        }
        return getValue(strValue, enumClass);
    }
}
