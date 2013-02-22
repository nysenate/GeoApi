package gov.nysenate.sage.filter;

import static gov.nysenate.sage.controller.api.base.RequestAttribute.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import gov.nysenate.sage.controller.api.base.RequestAttribute;
import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.ApiErrorResult;
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
 * ApiFilter serves common functions that each api controller method requires.
 * This includes:
 *                Filter API requests based on IP address and API key
 *                Perform URI validation
 *                Output format processing and response writing
 *                Gracefully setting error messages in the response
 *
 * This filter processes both the request propagating to the controllers as well
 * as the response coming back from the controllers.
 *
 * @author Jared Williams, Ash Islam
 */
public class ApiFilter implements Filter, Observer
{
    private Logger logger = Logger.getLogger(ApiFilter.class);
    private Config config;
    private String ipFilter;
    private String defaultKey;

    public static String INVALID_KEY_MESSAGE = "Authentication Error - Supplied key could not be validated.";
    public static String MISSING_KEY_MESSAGE = "Authentication Error - Please provide a valid API key. ( Set parameter key=API_KEY )";
    public static String INVALID_API_FORMAT = "Format Error - Invalid request. Please check the documentation for proper API format";

    /** Available format types */
    public enum FormatTypes
    {
        JSON, XML;
    }

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

        /** Check that the uri is formatted correctly */
        if (parseUri(uri, request, response)){

            /** The filter will proceed to the next chain only if the user has a valid key or is the default user.
             *  Otherwise an appropriate error message will be written in the format specified by the request. */
            if (authenticateUser(key, remoteIp, uri, request, response)) {

                    /** Proceed to next filter or servlet */
                    chain.doFilter(request, response);

                    /** Response from chain percolates back through here */
                    this.doResponseFilter(request, response);
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
                sendApiError(INVALID_KEY_MESSAGE, request, response);
            }
        }
        else {
            logger.debug(String.format("No key supplied to access %s from %s", uri, remoteIp));
            sendApiError(MISSING_KEY_MESSAGE, request, response);
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

            uriTokens.put(API_TYPE, tokenizer.nextToken());
            uriTokens.put(FORMAT, tokenizer.nextToken());

            /** Check for optional '/body/' in uri */
            String token = tokenizer.nextToken();
            if (token != null && token.equalsIgnoreCase("body")){
                uriTokens.put(PARAM_SOURCE, "body");
                if (tokenizer.hasMoreTokens()){
                    uriTokens.put(REQUEST_TYPE, tokenizer.nextToken().replaceFirst("(\\?.*)", ""));
                }
                else {
                    throw new NoSuchElementException("Missing " + PARAM_SOURCE.toString());
                }
            }
            else {
                uriTokens.put(PARAM_SOURCE, "url");
                if (token != null){
                    uriTokens.put(REQUEST_TYPE, token.replaceFirst("(\\?.*)", ""));
                }
                else {
                    throw new NoSuchElementException("Missing " + REQUEST_TYPE.toString());
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

        sendApiError(INVALID_API_FORMAT, request, response);
        return false;
    }

    /**
     * Creates an error result and writes it to the response.
     * @throws IOException
     */
    private void sendApiError(String message, ServletRequest request, ServletResponse response) throws IOException
    {
        String format = (String) request.getAttribute("format");
        ApiErrorResult errorResponse = new ApiErrorResult(message);
        message = formatResultMap(errorResponse.toMap(), format, message);
        writeResponse(message, response);
    }

    /**
     * Writes a string to the response.
     * @param message
     * @param response
     * @throws IOException
     */
    private void writeResponse(String message, ServletResponse response) throws IOException
    {
        if (response != null && message != null){
            response.getWriter().write(message);
        }
    }

    /**
     * Transforms a map into either a JSON or XML string.
     * @param messageMap     The Map containing the data to be serialized
     * @param format         One of the FormatType formats
     * @param defaultMessage Default message if formatting fails
     * @return              Serialized string according to format, or defaultMessage
     *                      if format was invalid or processing error.
     */
    private String formatResultMap(Map<String, Object> messageMap, String format, String defaultMessage)
    {
        if (messageMap != null){
            if (format != null){
                try {
                    if (format.equalsIgnoreCase(FormatTypes.JSON.name())){
                        return FormatUtil.mapToJson(messageMap);
                    }
                    else if (format.equalsIgnoreCase(FormatTypes.XML.name())){
                        return FormatUtil.mapToXml(messageMap, "response", true);
                    }
                }
                catch (JsonProcessingException ex){
                    logger.error("Failed to format response into " + format);
                    logger.error(ex.getMessage());
                }
            }
        }
        return defaultMessage;
    }

    /**
     * Perform final tasks as the servlet response percolates back up the filter chain.
     */
    private void doResponseFilter(ServletRequest request, ServletResponse response) throws IOException
    {
        Map<String,Object> map = (Map<String,Object>) request.getAttribute("response");
        String format = (String) request.getAttribute("format");
        String resp = formatResultMap(map, format, "Unknown format");
        writeResponse(resp, response);
    }

    @Override
    public void destroy() {}

    public void update(Observable o, Object arg)
    {
        configure();
    }
}