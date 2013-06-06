package gov.nysenate.sage.controller.error;

import gov.nysenate.sage.controller.api.BaseApiController;
import org.apache.log4j.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ExceptionController extends BaseApiController
{
    private static Logger logger = Logger.getLogger(ExceptionController.class);

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Exception ex = (Exception) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        logger.fatal(ex.toString());
        logger.fatal(request.getAttribute(RequestDispatcher.ERROR_MESSAGE));
        logger.fatal(request.getAttribute(RequestDispatcher.ERROR_SERVLET_NAME));
        logger.fatal(request.getAttribute(RequestDispatcher.ERROR_EXCEPTION_TYPE));
        response.getWriter().write("MOOOOO");
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
