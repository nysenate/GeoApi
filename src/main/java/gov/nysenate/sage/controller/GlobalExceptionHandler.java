package gov.nysenate.sage.controller;

import gov.nysenate.sage.client.response.base.ApiError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static gov.nysenate.sage.model.result.ResultStatus.INTERNAL_ERROR;

/**
 * Catches any exception not handled by a more specific handler so the client always receives a
 * JSON error response instead of a raw servlet error page.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleException(Exception e) {
        logger.error("Unhandled exception while processing request", e);
        return new ApiError(INTERNAL_ERROR);
    }
}