package gov.nysenate.sage.filter;

import gov.nysenate.sage.dao.logger.ExceptionLogger;
import org.apache.log4j.Logger;

import javax.servlet.*;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;

public class ExceptionFilter implements Filter
{
    private static Logger logger = Logger.getLogger(ExceptionFilter.class);
    private static ExceptionLogger exceptionLogger;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        exceptionLogger = new ExceptionLogger();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
    {
        try {
            chain.doFilter(request, response);
        }
        catch (Exception ex) {
            logger.fatal("Logging uncaught exception!", ex);
            Integer apiRequestId = (Integer) request.getAttribute("apiRequestId");
            exceptionLogger.logException(ex, new Timestamp(new Date().getTime()), apiRequestId);
        }
    }

    @Override
    public void destroy() {}
}
