package gov.nysenate.sage.filter;

import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.ApiErrorResponse;
import gov.nysenate.sage.model.auth.ApiUser;
import gov.nysenate.sage.util.FormatUtil;
import gov.nysenate.sage.util.ApiUserAuth;
import gov.nysenate.sage.util.Config;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

/**
 * Filters API requests based on IP address and API key.
 *
 * @author Jared Williams
 */
public class ApiFilter implements Filter, Observer {

    private Logger logger;
    private Config config;
    private String ipFilter;
    private String apiFilter;
    private String defaultKey;

    public static String INVALID_KEY_MESSAGE = "Supplied key could not be validated.";
    public static String MISSING_KEY_MESSAGE = "Please provide a valid API key. ( Set parameter key=API_KEY )";



    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
        logger = Logger.getLogger(this.getClass());
        config = ApplicationFactory.getConfig();
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

        if (key == null && remote_ip.matches(ipFilter))
        {
            key = defaultKey;
            logger.debug(String.format("Default user: %s granted default key %s", remote_ip, key));
        }

        if (key != null)
        {
            ApiUserAuth apiUserAuth = new ApiUserAuth();
            ApiUser apiUser = apiUserAuth.getApiUser(key);
            if (apiUser != null)
            {
                logger.debug(String.format("ApiUser %s has been authenticated successfully", apiUser.getName()));
                request.setAttribute("apiUser", apiUser);
                chain.doFilter(request, response);
            }
            else
            {
                logger.debug(String.format("Failed to validate request to %s from %s using key: %s", uri, remote_ip, key));
                writeApiAuthError(INVALID_KEY_MESSAGE, request,response);
            }
        }
        else
        {
            logger.debug(String.format("No key supplied to access %s from %s", uri, remote_ip));
            writeApiAuthError(MISSING_KEY_MESSAGE, request, response);
        }
    }

    private void writeApiAuthError(String message, ServletRequest request, ServletResponse response) throws IOException
    {
         message = "Api Authentication Error: " + message;
         String format = request.getParameter("format");
         if ( format != null )
         {
             if (format.equals("json"))
             {
                 ApiErrorResponse errorResponse = new ApiErrorResponse(message);
                 message = FormatUtil.toJsonString(errorResponse.toMap());
             }
         }
         response.getWriter().write(message);
    }

    @Override
    public void destroy() {}

    public void update(Observable o, Object arg)
    {
        configure();
    }
}