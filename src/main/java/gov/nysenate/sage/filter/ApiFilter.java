package gov.nysenate.sage.filter;

import static gov.nysenate.sage.controller.api.RequestAttribute.*;

import gov.nysenate.sage.controller.api.RequestAttribute;
import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.ApiErrorResponse;
import gov.nysenate.sage.model.auth.ApiUser;
import gov.nysenate.sage.util.FormatUtil;
import gov.nysenate.sage.util.ApiUserAuth;
import gov.nysenate.sage.util.Config;
import java.io.IOException;
import java.util.*;
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
 * Performs URI validation and output format processing.
 *
 * @author Jared Williams, Ash Islam
 */
public class ApiFilter implements Filter, Observer {

    private Logger logger = Logger.getLogger(ApiFilter.class);
    private Config config;
    private String ipFilter;
    private String defaultKey;

    public static String INVALID_KEY_MESSAGE = "Supplied key could not be validated.";
    public static String MISSING_KEY_MESSAGE = "Please provide a valid API key. ( Set parameter key=API_KEY )";
    public static String INVALID_API_FORMAT = "Invalid request. Please check the documentation for proper API format";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
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
        String remoteIp = request.getRemoteAddr();
        String uri = ((HttpServletRequest)request).getRequestURI();

        /** The filter will proceed to the next chain only if the user has a valid key or is the default user.
         *  Otherwise an appropriate error message will be written in the format specified by the request. */
        if (authenticateUser(key, remoteIp, uri, request, response)) {

            /** Check that the uri is formatted correctly */
            if (parseUri(uri, request, response)){

                /** Proceed to next filter or servlet */
                chain.doFilter(request, response);

                /** Response from chain percolates back through here to perform logging/formatting */
                doPostFilter();
            }
        }
    }

    /**
     * If the user authenticates using their key or IP address then an "apiUser" attribute
     * will be set in the ServletRequest with the matched ApiUser object. If the user does
     * not authenticate then the appropriate error messages will be written to the response.
     *
     * @param key       Supplied API key
     * @param remoteIp  IP address of requester
     * @param uri       Request URI
     * @param request
     * @param response
     * @return          true if authenticated, false otherwise
     * @throws IOException
     */
    private boolean authenticateUser(String key, String remoteIp, String uri,
                                     ServletRequest request, ServletResponse response ) throws IOException
    {
        if (key == null && remoteIp.matches(ipFilter)) {
            key = defaultKey;
            logger.debug(String.format("Default user: %s granted default key %s", remoteIp, key));
        }

        if (key != null) {
            ApiUserAuth apiUserAuth = new ApiUserAuth();
            ApiUser apiUser = apiUserAuth.getApiUser(key);

            if (apiUser != null) {
                logger.debug(String.format("ApiUser %s has been authenticated successfully", apiUser.getName()));
                request.setAttribute("apiUser", apiUser);
                return true;
            }
            else {
                logger.debug(String.format("Failed to validate request to %s from %s using key: %s", uri, remoteIp, key));
                sendApiAuthError(INVALID_KEY_MESSAGE, request, response);
            }
        }
        else {
            logger.debug(String.format("No key supplied to access %s from %s", uri, remoteIp));
            sendApiAuthError(MISSING_KEY_MESSAGE, request, response);
        }
        return false;
    }

    /**
     * Parses the URI to obtain the API attributes. Sets them as request attributes so they
     * can be accessed by the controllers.
     * @param uri
     * @param request
     * @return  true if api parsed correctly
     *          false otherwise
     */
    private boolean parseUri(String uri, ServletRequest request, ServletResponse response) throws IOException
    {
        HashMap<RequestAttribute, String> uriTokens = new LinkedHashMap<>();
        try {
            StringTokenizer tokenizer = new StringTokenizer(uri, "/");

            /** Eat the string once or twice until we're past the /api/ portion.
             *  This is to accommodate if the context name is in the uri string. */
            if (!tokenizer.nextToken().equalsIgnoreCase("api")){
                tokenizer.nextToken();
            }

            uriTokens.put(REQUEST_TYPE, tokenizer.nextToken());
            uriTokens.put(FORMAT, tokenizer.nextToken());

            /** Check for optional '/body/' in uri */
            String token = tokenizer.nextToken();
            if (token != null && token.equalsIgnoreCase("body")){
                uriTokens.put(PARAM_SOURCE, "body");
                if (tokenizer.hasMoreTokens()){
                    uriTokens.put(PARAM_TYPE, tokenizer.nextToken().replaceFirst("(\\?.*)", ""));
                }
                else {
                    throw new NoSuchElementException("Missing " + PARAM_SOURCE.toString());
                }
            }
            else {
                uriTokens.put(PARAM_SOURCE, "url");
                if (token != null){
                    uriTokens.put(PARAM_TYPE, token.replaceFirst("(\\?.*)", ""));
                }
                else {
                    throw new NoSuchElementException("Missing " + PARAM_TYPE.toString());
                }
            }

            /** Store all the tokens as request attributes. */
            for (RequestAttribute attr : uriTokens.keySet()){
                request.setAttribute(attr.toString(), uriTokens.get(attr));
            }

            return true;
        }
        catch (NullPointerException ex){
            logger.debug(uri + " is not formatted as a valid api request. " + ex.getMessage());
        }
        catch (NoSuchElementException ex){
            logger.debug(uri + " is not formatted as a valid api request. " + ex.getMessage());
        }
        sendApiAuthError(INVALID_API_FORMAT, request, response);
        return false;
    }

    private void sendApiAuthError(String message, ServletRequest request, ServletResponse response) throws IOException
    {
         message = "Api Authentication Error: " + message;
         String format = request.getParameter("format");
         if ( format != null ) {
             if (format.equals("json")) {
                 ApiErrorResponse errorResponse = new ApiErrorResponse(message);
                 message = FormatUtil.toJsonString(errorResponse.toMap());
             }
         }
         response.getWriter().write(message);
    }

    /**
     * Perform final tasks as the servlet response percolates back up the filter chain.
     */
    private void doPostFilter()
    {
        logger.debug("Percolating back up!");
    }

    @Override
    public void destroy() {}

    public void update(Observable o, Object arg)
    {
        configure();
    }
}