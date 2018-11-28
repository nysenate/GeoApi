package gov.nysenate.sage.filter;

import gov.nysenate.sage.dao.logger.SqlExceptionLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import javax.servlet.*;
import java.sql.Timestamp;
import java.util.Date;

@Component
public class ExceptionFilter implements Filter
{
    private static Logger logger = LoggerFactory.getLogger(ExceptionFilter.class);
    Marker fatal = MarkerFactory.getMarker("FATAL");
    private SqlExceptionLogger sqlExceptionLogger;

    @Autowired
    public ExceptionFilter(SqlExceptionLogger sqlExceptionLogger) {
        this.sqlExceptionLogger = sqlExceptionLogger;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
    {
        try {
            chain.doFilter(request, response);
        }
        catch (Exception ex) {
            logger.error(fatal, "Logging uncaught exception!", ex);
            Integer apiRequestId = (Integer) request.getAttribute("apiRequestId");
            sqlExceptionLogger.logException(ex, new Timestamp(new Date().getTime()), apiRequestId);
        }
    }

    @Override
    public void destroy() {}
}
