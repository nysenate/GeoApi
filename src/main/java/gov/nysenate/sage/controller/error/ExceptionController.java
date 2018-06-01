package gov.nysenate.sage.controller.error;

import gov.nysenate.sage.dao.logger.ExceptionLogger;
import gov.nysenate.sage.util.controller.ConstantUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;

@Controller

public class ExceptionController
{
    private static Logger logger = LogManager.getLogger(ExceptionController.class);
    private ExceptionLogger exceptionLogger;

    @Autowired
    public ExceptionController(ExceptionLogger exceptionLogger) {
        this.exceptionLogger = exceptionLogger;
    }

    @RequestMapping(value ="/exception")
    public void doGet(HttpServletRequest request, HttpServletResponse response, @RequestParam int apiRequestId) throws IOException
    {
        Exception ex = (Exception) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        if (ex != null) {
            exceptionLogger.logException(ex, new Timestamp(new Date().getTime()), apiRequestId);
        }
        logger.fatal("Unhandled exception occurred!", ex);
        response.sendError(500, "An unexpected application error has occurred. The administrators have been notified. Please try again later.");
    }
}
