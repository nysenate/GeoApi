package gov.nysenate.sage.servlets;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * @author Jared Williams
 *
 * filers application requests based on ip address and api requests based on ip
 * and api key
 */
public class SenateFilter implements Filter {
    private Logger logger;
    private final String SENATE_IP_RANGE = "(10.\\d+.\\d+.\\d+|127.0.0.1|0:0:0:0:0:0:0:1|63.118.5[67].\\d+)";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // Requests from outside the SENATE_IP_RANGE are redirected unless they
        //  a. Supply a key to an API request [key is validated elsewhere]
        //  b. Are requesting a GeoAPI/maps/* resource
        String key = request.getParameter("key");
        String uri = ((HttpServletRequest)request).getRequestURI();
        String remote_ip = request.getRemoteAddr();

        logger.debug(String.format("%s - %s",remote_ip, uri));
        if (!remote_ip.matches(SENATE_IP_RANGE)) {

            if (uri.matches("(/GeoApi)?/api/.*") && key==null) {
                logger.debug("API Request Denied: Offsite Request Missing Key");
                ((HttpServletResponse)response).sendRedirect("http://www.nysenate.gov");
                return;
            }

            if (!uri.matches("(/GeoApi)?(/maps/.*?)")) {
                logger.debug("Application Request Denied. Offsite Request.");
                ((HttpServletResponse)response).sendRedirect("http://www.nysenate.gov");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger = Logger.getLogger(this.getClass());
    }

    @Override
    public void destroy() {}
}
