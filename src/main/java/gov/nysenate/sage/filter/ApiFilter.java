package gov.nysenate.sage.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import gov.nysenate.sage.client.api.ApiError;
import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.api.ApiRequest;
import gov.nysenate.sage.model.api.ApiUser;
import gov.nysenate.sage.util.ApiUserAuth;
import gov.nysenate.sage.util.Config;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import static gov.nysenate.sage.model.result.ResultStatus.*;

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
 * @author Ash Islam
 */
public class ApiFilter implements Filter, Observer
{
    private Logger logger = Logger.getLogger(ApiFilter.class);
    private Config config;
    private String ipFilter;
    private String defaultKey;

    /** The valid format of an api request */
    private static String validFormat = "((?<context>.*)\\/)?api\\/v(?<version>\\d+)\\/(?<service>(address|district|geo|map))\\/(?<request>\\w+)(\\/(?<batch>batch))?";

    /** String keys used for setting attributes */
    private static final String responseObjectKey = "responseObject";
    private static final String formattedResponseKey = "formattedResponse";
    private static final String apiRequestKey = "apiRequest";

    /** Available format types */
    public enum FormatType { JSON, XML, JSONP; }

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

    public void update(Observable o, Object arg)
    {
        configure();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
    {
        String key = request.getParameter("key");
        String remoteIp = request.getRemoteAddr();
        String uri = ((HttpServletRequest)request).getRequestURI();

        /** Check that the url is formatted correctly */
        if (validateRequest(uri, request, response)){
            /** The filter will proceed to the next chain only if the user has a valid key or is the default user.
             *  Otherwise an error message will be sent. */
            if (authenticateUser(key, remoteIp, uri, request, response)) {
                chain.doFilter(request, response);
            }
        }

        /** Response from chain percolates to here */
        formatResponse(request, response);
        sendResponse(request, response);
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
                setResponse(new ApiError(API_KEY_INVALID), request);
                logger.debug(String.format("Failed to validate request to %s from %s using key: %s", uri, remoteIp, key));
            }
        }
        else {
            setResponse(new ApiError(API_KEY_MISSING), request);
            logger.debug(String.format("No key supplied to access %s from %s", uri, remoteIp));
        }
        return false;
    }

    /**
     * Parses the URI to obtain the API attributes. Sets them as request attributes so they
     * can be accessed by the controllers. Also checks to see if the output format is valid.
     * @param uri
     * @param request
     * @return  true if api parsed correctly
     *          false otherwise
     */
    private boolean validateRequest(String uri, ServletRequest request, ServletResponse response) throws IOException
    {
        Pattern validFormatPattern = Pattern.compile(validFormat);
        Matcher matcher = validFormatPattern.matcher(uri);

        /** Validate the output format only if it is set. It is okay if the format is not specified. */
        String outputFormat = request.getParameter("format");
        if (outputFormat != null){
            boolean validOutputFormat = false;
            for (FormatType formatType : FormatType.values()) {
                if (outputFormat.equalsIgnoreCase(formatType.name())) {
                    validOutputFormat = true; break;
                }
            }
            if (!validOutputFormat) {
                setResponse(new ApiError(API_OUTPUT_FORMAT_UNSUPPORTED), request);
                return false;
            }
            if (outputFormat.equalsIgnoreCase(FormatType.JSONP.name()) && request.getParameter("callback") == null) {
                setResponse(new ApiError(JSONP_CALLBACK_NOT_SPECIFIED), request);
                return false;
            }
        }

        /** If the url pattern matches, then obtain the parameters and propagate an ApiResult object as an
         *  attribute with the key 'apiRequest'. */
        if (matcher.find()) {

            int version = Integer.valueOf(matcher.group("version"));
            String service = matcher.group("service");
            String req = matcher.group("request");
            boolean batch = (matcher.group("batch") != null);
            setApiRequest(new ApiRequest(version, service, req, batch), request);

            return true;
        }
        else {
            setResponse(new ApiError(API_REQUEST_INVALID), request);
            return false;
        }
    }

    private static void setApiRequest(ApiRequest apiRequest, ServletRequest request)
    {
        request.setAttribute(apiRequestKey, apiRequest);
    }

    /** Accessor to ApiRequest object stored in ServletRequest */
    public static ApiRequest getApiRequest(ServletRequest request)
    {
        return (ApiRequest) request.getAttribute(apiRequestKey);
    }

    /**
     * Simply sets the given response object as an attribute within the request. This is used
     * for passing response data to the formatting methods for output processing.
     * @param response  Object containing response data
     * @param request   ServletRequest
     */
    public static void setResponse(Object response, ServletRequest request)
    {
        request.setAttribute(responseObjectKey, response);
    }

    /** Obtains the response object and serializes it using the format specified in the request parameters.
     *  The default output format is JSON. The default format will be used in the following cases:
     *  - No format specified in the parameters.
     *  - Invalid format specified in the parameters.
     * @param request   ServletRequest
     */
    private void formatResponse(ServletRequest request, ServletResponse response)
    {
        String format = request.getParameter("format");
        if (format == null) {
            format = FormatType.JSON.name();
        }

        Object responseObj = request.getAttribute(responseObjectKey);

        /** Set a response error if the response object attribute is not set */
        if (responseObj == null) {
            responseObj = new ApiError(RESPONSE_ERROR);
        }

        ObjectMapper jsonMapper = new ObjectMapper();
        jsonMapper.enable(SerializationFeature.INDENT_OUTPUT);

        try {
            if (format.equalsIgnoreCase(FormatType.XML.name())) {
                XmlMapper xmlMapper = new XmlMapper();
                xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
                String xml = xmlMapper.writeValueAsString(responseObj);
                request.setAttribute(formattedResponseKey, xml);
                response.setContentType("application/xml");
                response.setContentLength(xml.length());
            }
            else if (format.equalsIgnoreCase(FormatType.JSONP.name())) {
                String callback = request.getParameter("callback");
                String json = jsonMapper.writeValueAsString(responseObj);
                String jsonp = String.format("%s(%s);", callback, json);
                request.setAttribute(formattedResponseKey, jsonp);
                response.setContentType("application/javascript");
                response.setContentLength(jsonp.length());
            }
            else {
                String json = jsonMapper.writeValueAsString(responseObj);
                request.setAttribute(formattedResponseKey, json);
                response.setContentType("application/json");
                response.setContentLength(json.length());
            }
        }
        catch (JsonProcessingException ex) {
            logger.fatal("Failed to serialize response!", ex);
            request.setAttribute(formattedResponseKey, RESPONSE_SERIALIZATION_ERROR);
        }
    }

    /**
     * Writes the formatted response to the output stream.
     * @param request   ServletRequest
     * @param response  ServletResponse
     */
    private void sendResponse(ServletRequest request, ServletResponse response)
    {
        Object formattedResponse = request.getAttribute(formattedResponseKey);
        try {
            if (formattedResponse != null) {
                response.getWriter().write(formattedResponse.toString());
            }
            else {
                logger.error("No formatted response set!");
                response.getWriter().write(RESPONSE_ERROR.getDesc());
            }
        }
        catch (IOException ex){
            logger.error("Failed to write to output stream!", ex);
        }
    }

    @Override
    public void destroy() {}
}