package gov.nysenate.sage.servlets;

import gov.nysenate.sage.util.Config;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

/**
 * @author Jared Williams
 *
 * filers application requests based on ip address and api requests based on ip
 * and api key
 */
@Deprecated
public class SenateFilter implements Filter, Observer {
    private Logger logger;

    private String ipFilter;
    private String apiFilter;
    private String defaultKey;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger = Logger.getLogger(this.getClass());
        Config.notify(this);
        ServletContext servletContext = filterConfig.getServletContext();
        String contextPath = servletContext.getContextPath();

        // Requires Tomcat 7
        // Collection<String> apiMappings = servletContext.getServletRegistration("ApiServlet").getMappings();
        // apiFilter = contextPath+"("+StringUtils.join(apiMappings,"|")+").*";
        apiFilter = contextPath+"/api/.*";
        logger.debug("Challenging requests for: "+apiFilter);
        configure();
    }

    public void configure() {
        ipFilter = Config.read("user.ip_filter");
        defaultKey = Config.read("user.default");
        logger.debug(String.format("Allowing access on %s via %s",ipFilter,defaultKey));
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String key = request.getParameter("key");
        String remote_ip = request.getRemoteAddr();
        String uri = ((HttpServletRequest)request).getRequestURI();
        if (key != null)
            logger.debug(String.format("%s from %s using %s",uri, remote_ip,  key));
        else
            logger.debug(String.format("%s from %s using %s",uri, remote_ip,  "no key"));

        // Challenge API Requests to authorize
        if (uri.matches(apiFilter)) {

            // Special IPs can use the default user
            logger.debug(String.format("User key is '%s'",key));
            if (key == null && remote_ip.matches(ipFilter)) {
                key = defaultKey;
                logger.debug("Using default user: "+key);
            }

            request.setAttribute("api_key", key);
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}

    public void update(Observable o, Object arg) {
        configure();
    }
}
