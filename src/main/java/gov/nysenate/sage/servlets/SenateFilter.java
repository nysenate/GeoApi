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

/**
 * @author Jared Williams
 *
 * filers application requests based on ip address and api requests based on ip
 * and api key
 */
public class SenateFilter implements Filter {

    private final String SENATE_IP_RANGE = "(10.\\d+.\\d+.\\d+|127.0.0.1|63.118.5[67].\\d+)";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // Requests from outside the SENATE_IP_RANGE are redirected unless they
        //  a. Supply a key to an API request [key is validated elsewhere]
        //  b. Are requesting a GeoAPI/maps/* resource
        if (!request.getRemoteAddr().matches(SENATE_IP_RANGE)) {
            String key = request.getParameter("key");
            String uri = ((HttpServletRequest)request).getRequestURI();

            if (uri.matches("(/GeoApi)?/api/.*") && key==null) {
                ((HttpServletResponse)response).sendRedirect("http://www.nysenate.gov");
                return;
            }

            if (!uri.matches("(/GeoApi)?(/maps/.*?)")) {
                ((HttpServletResponse)response).sendRedirect("http://www.nysenate.gov");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}
}
