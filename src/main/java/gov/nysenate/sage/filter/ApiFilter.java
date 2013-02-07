package gov.nysenate.sage.filter;

import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.util.Config;
import java.io.IOException;
import java.util.Collection;
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

import gov.nysenate.sage.util.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * Filers application requests based on ip address and api requests based on ip
 * and api key.
 *
 * @author Jared Williams
 */
public class ApiFilter implements Filter, Observer {

    private Logger logger;
    private Config config;
    private String ipFilter;
    private String apiFilter;
    private String defaultKey;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
        logger = Logger.getLogger(this.getClass());
        config = ApplicationFactory.getConfig();

        ServletContext servletContext = filterConfig.getServletContext();
        String contextPath = servletContext.getContextPath();

        apiFilter = contextPath+"/api/.*";
        logger.debug("Challenging requests for: " + apiFilter);

        configure();
    }

    public void configure()
    {
        ipFilter = config.getValue("user.ip_filter");
        defaultKey = config.getValue("user.default");
        logger.debug(String.format("Allowing access on %s via default key %s", ipFilter, defaultKey));
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
    {
        String key = request.getParameter("key");
        String remote_ip = request.getRemoteAddr();
        String uri = ((HttpServletRequest)request).getRequestURI();

        if (key != null)
        {
            logger.debug(String.format("%s from %s using %s", uri, remote_ip, key));
        }
        else
        {
            /** Special IPs can use the default user */
            if (key == null && remote_ip.matches(ipFilter))
            {
                key = defaultKey;
                logger.debug(String.format("Default user: %s granted default key %s", remote_ip, key));
            }
            else
            {
                logger.debug(String.format("No key found for %s when trying to access %s", remote_ip, uri));
            }
        }

        request.setAttribute("api_key", key);
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}

    public void update(Observable o, Object arg)
    {
        configure();
    }
}
