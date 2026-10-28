package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.base.ApiError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import static gov.nysenate.sage.model.result.ResultStatus.INTERNAL_ERROR;

public class BaseApiController {
    private static final Logger logger = LoggerFactory.getLogger(BaseApiController.class);

    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleException(Exception e) {
        logger.error("Unhandled exception while processing request", e);
        return new ApiError(INTERNAL_ERROR);
    }
}
