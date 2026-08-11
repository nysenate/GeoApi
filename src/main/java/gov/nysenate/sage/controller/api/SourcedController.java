package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.client.response.district.DisplayEnumResponse;
import gov.nysenate.sage.model.district.NoShapefileForDistrictTypeException;
import gov.nysenate.sage.util.HasDisplayName;
import org.springframework.core.GenericTypeResolver;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static gov.nysenate.sage.model.result.ResultStatus.MISSING_DISTRICT_CODE;
import static gov.nysenate.sage.model.result.ResultStatus.UNSUPPORTED_DISTRICT_MAP;

public abstract class SourcedController<E extends Enum<E> & HasDisplayName> extends BaseApiController {
    @SuppressWarnings("unchecked")
    private final Class<E> sourceClass = Objects.requireNonNull(
            (Class<E>) GenericTypeResolver.resolveTypeArgument(getClass(), SourcedController.class)
    );

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

    /**
     * A request parameter that couldn't be converted to its declared type is a bad request, not a
     * server error. Without this, BaseApiController's catch-all would report it as a 500.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ApiError handleTypeMismatchException(MethodArgumentTypeMismatchException e) {
        return new ApiError(MISSING_DISTRICT_CODE);
    }

    protected <T extends Enum<T>> T getValue(Class<T> enumClass, String strValue) {
        try {
            return T.valueOf(enumClass, strValue.trim().toUpperCase());
        } catch (NullPointerException | IllegalArgumentException e) {
            throw new InvalidValueException(getClass());
        }
    }

    protected E getValue(String strValue) {
        return getValue(sourceClass, strValue);
    }

    protected <T extends Enum<T>> List<T> getListOrNull(Class<T> enumClass, String strValue) {
        if (strValue == null || strValue.isBlank()) {
            return null;
        }
        return List.of(getValue(enumClass, strValue));
    }

    protected List<E> getListOrNull(String strValue) {
        return getListOrNull(sourceClass, strValue);
    }

    /**
     * Lists the available options for this controller. Inherited by every subclass, so each
     * concrete controller exposes this under its own base path.
     */
    @GetMapping(value = "/options")
    public List<DisplayEnumResponse> options() {
        return Stream.of(sourceClass.getEnumConstants()).map(DisplayEnumResponse::new).toList();
    }
}
