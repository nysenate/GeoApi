package gov.nysenate.sage.controller.error;

import gov.nysenate.sage.dao.logger.exception.SqlExceptionLogger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;

import static gov.nysenate.sage.util.controller.ConstantUtil.ADMIN_REST_PATH;

@Controller
public class ExceptionController
{
    private static Logger logger = LoggerFactory.getLogger(ExceptionController.class);
    private SqlExceptionLogger sqlExceptionLogger;
    Marker fatal = MarkerFactory.getMarker("FATAL");

    @Autowired
    public ExceptionController(SqlExceptionLogger sqlExceptionLogger) {
        this.sqlExceptionLogger = sqlExceptionLogger;
    }

    /**
     * Get Exception Api
     * ---------------------
     *
     * Get an exception from an ApiRequestId
     *
     * Usage:
     * (GET)    /exception
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param apiRequestId int
     * @throws IOException
     *
     */
    @RequestMapping(value ="/exception")
    public void doGet(HttpServletRequest request, HttpServletResponse response, @RequestParam int apiRequestId) throws IOException
    {
        Exception ex = (Exception) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        if (ex != null) {
            sqlExceptionLogger.logException(ex, new Timestamp(new Date().getTime()), apiRequestId);
        }
        logger.error(fatal,"Unhandled exception occurred!", ex);
        response.sendError(500, "An unexpected application error has occurred. The administrators have been notified. Please try again later.");
    }
}
