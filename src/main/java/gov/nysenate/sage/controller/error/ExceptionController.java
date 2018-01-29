package gov.nysenate.sage.controller.error;

import gov.nysenate.sage.dao.logger.ExceptionLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;

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
public class ExceptionController extends HttpServlet
{
    private static Logger logger = LogManager.getLogger(ExceptionController.class);
    private ExceptionLogger exceptionLogger;

    @Override
    public void init(ServletConfig config) throws ServletException {
        exceptionLogger = new ExceptionLogger();
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        Integer apiRequestId = (Integer) request.getAttribute("apiRequestId");
        Exception ex = (Exception) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        if (ex != null) {
            exceptionLogger.logException(ex, new Timestamp(new Date().getTime()), apiRequestId);
        }
        logger.fatal("Unhandled exception occurred!", ex);
        response.sendError(500, "An unexpected application error has occurred. The administrators have been notified. Please try again later.");
    }
}
