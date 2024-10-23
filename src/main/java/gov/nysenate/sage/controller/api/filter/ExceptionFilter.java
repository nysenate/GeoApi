package gov.nysenate.sage.controller.api.filter;

import gov.nysenate.sage.dao.logger.exception.SqlExceptionLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import java.sql.Timestamp;
import java.util.Date;

@Component
public class ExceptionFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionFilter.class);
    private static final Marker fatal = MarkerFactory.getMarker("FATAL");
    private final SqlExceptionLogger sqlExceptionLogger;

    @Autowired
    public ExceptionFilter(SqlExceptionLogger sqlExceptionLogger) {
        this.sqlExceptionLogger = sqlExceptionLogger;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        try {
            chain.doFilter(request, response);
        }
        catch (Exception ex) {
            logger.error(fatal, "Logging uncaught exception!", ex);
            Integer apiRequestId = (Integer) request.getAttribute("apiRequestId");
            sqlExceptionLogger.logException(ex, new Timestamp(new Date().getTime()), apiRequestId);
        }
    }
}
