package gov.nysenate.sage.controller.api.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.dao.logger.apirequest.SqlApiRequestLogger;
import gov.nysenate.sage.model.api.ApiRequest;
import gov.nysenate.sage.model.api.ApiUser;
import gov.nysenate.sage.util.auth.ApiUserAuth;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static gov.nysenate.sage.model.result.ResultStatus.*;

/**
 * ApiFilter serves common functions that each api controller method requires.
 * This includes:
 *                Authentication
 *                URI validation
 *                Request pre-processing
 *                Response processing
 *
 * This filter processes both the request propagating to the controllers as well
 * as the response coming back from the controllers.
 *
 * @author Ash Islam
 */
@Component("apiFilter")
public class ApiFilter implements Filter
{
    private static Logger logger = LoggerFactory.getLogger(ApiFilter.class);
    Marker fatal = MarkerFactory.getMarker("FATAL");
    private SqlApiRequestLogger sqlApiRequestLogger;
    private ApiUserAuth apiUserAuth;
    private static String ipFilter;
    private static String defaultKey;
    private static String publicKey;

    private static Boolean API_LOGGING_ENABLED = false;

    /** The valid format of an api request */
    private static String validFormat = "((?<context>.*)\\/)?api\\/v(?<version>\\d+)\\/(?<service>(address|district|geo|map|street|meta|data))\\/(?<request>\\w+)(\\/(?<batch>batch))?";

    /** Api services that are designated as public */
    private static String publicApiFilter = "(map)";

    /** Api services for which requests will be logged if logging is enabled. */
    private static String loggedServices = "(address|district|geo|map|street|data)";

    /** String keys used for setting key value attributes in the request object */
    private static final String RESPONSE_OBJECT_KEY = "responseObject";
    private static final String FORMATTED_RESPONSE_KEY = "formattedResponse";
    private static final String API_REQUEST_KEY = "apiRequest";
    private static final String API_USER_KEY = "apiUser";

    /** Serializers */
    private static ObjectMapper jsonMapper = new ObjectMapper();
    private static XmlMapper xmlMapper = new XmlMapper();

    /** Available format types */
    public enum FormatType { JSON, XML, JSONP }


    Environment env;

    @Autowired
    public ApiFilter(Environment env, SqlApiRequestLogger sqlApiRequestLogger, ApiUserAuth apiUserAuth) {
        this.env = env;
        this.sqlApiRequestLogger = sqlApiRequestLogger;
        this.apiUserAuth = apiUserAuth;
        ipFilter = env.getUserIpFilter();
        defaultKey = env.getUserDefaultKey();
        publicApiFilter = env.getPublicApiFilter();
        publicKey = env.getUserPublicKey();
        API_LOGGING_ENABLED = env.isApiLoggingEnabled();
        jsonMapper.enable(SerializationFeature.INDENT_OUTPUT);
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        configure();
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
    }

    public void configure()
    {
        logger.info(String.format("Configured default access on %s via key %s", ipFilter, defaultKey));
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException
    {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String key = servletRequest.getParameter("key");
        String forwardedForIp = request.getHeader("x-forwarded-for");
        String remoteIp = forwardedForIp == null ? request.getRemoteAddr() : forwardedForIp;

        String uri = request.getRequestURI();

        /** Check that the url is formatted correctly */
        if (validateRequest(uri, remoteIp, request)){
            /** The filter will proceed to the next chain only if the user has a valid key or is the default user.
             *  Otherwise an error message will be sent. */
            if (authenticateUser(key, remoteIp, uri, request)) {
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
     * @param request   ServletRequest
     * @return          true if authenticated, false otherwise
     * @throws IOException
     */
    private boolean authenticateUser(String key, String remoteIp, String uri, ServletRequest request) throws IOException
    {
        ApiRequest apiRequest = getApiRequest(request);
        String service = apiRequest.getService();

        if (key == null) {
            if (remoteIp.matches(ipFilter)) {
                key = defaultKey;
                logger.trace(String.format("Default user: %s granted default key %s", remoteIp, key));
            }
            else if (service.matches(publicApiFilter)) {
                key = publicKey;
                logger.trace(String.format("User: %s granted default map key for accessing public api.", remoteIp));
            }
        }

        if (key != null) {
            ApiUser apiUser = apiUserAuth.getApiUser(key);

            if (apiUser != null) {
                logger.trace(String.format("ApiUser %s has been authenticated successfully", apiUser.getName()));
                apiRequest.setApiUser(apiUser);

                /** Log Api Request into the database */
                int id = -1;
                if (API_LOGGING_ENABLED && service.matches(loggedServices)) {
                    id = sqlApiRequestLogger.logApiRequest(apiRequest);
                    apiRequest.setId(id);
                }

                /** Cache the current Api Request id */
                request.setAttribute("apiRequestId", id);
                return true;
            }
            else {
                setApiResponse(new ApiError(API_KEY_INVALID), request);
                logger.warn(String.format("Failed to validate request to %s from %s using key: %s", uri, remoteIp, key));
            }
        }
        else {
            setApiResponse(new ApiError(API_KEY_MISSING), request);
            logger.warn(String.format("No key supplied to access %s from %s", uri, remoteIp));
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
    private boolean validateRequest(String uri, String remoteIp, ServletRequest request) throws IOException
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
                setApiResponse(new ApiError(API_OUTPUT_FORMAT_UNSUPPORTED), request);
                return false;
            }
            /** Check for callback signature if format is JSONP */
            if (outputFormat.equalsIgnoreCase(FormatType.JSONP.name()) && request.getParameter("callback") == null) {
                setApiResponse(new ApiError(JSONP_CALLBACK_NOT_SPECIFIED), request);
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

            /** Resolve ip address into InetAddress */
            InetAddress remoteInetAddress = null;
            try {
                remoteInetAddress = InetAddress.getByName(remoteIp);
                logger.debug("Request from " + remoteInetAddress.getCanonicalHostName());
            }
            catch (UnknownHostException ex)
            {
                logger.warn("Unknown remote ip host!", ex);
            }

            /** Construct an ApiRequest object */
            ApiRequest apiRequest = new ApiRequest(version, service, req, batch, remoteInetAddress);
            apiRequest.setProvider(request.getParameter("provider"));

            setApiRequest(apiRequest, request);
            return true;
        }
        else {
            setApiResponse(new ApiError(API_REQUEST_INVALID), request);
            return false;
        }
    }

    private static void setApiRequest(ApiRequest apiRequest, ServletRequest request)
    {
        request.setAttribute(API_REQUEST_KEY, apiRequest);
    }

    /** Accessor to ApiRequest object stored in ServletRequest */
    public static ApiRequest getApiRequest(ServletRequest request)
    {
        return (ApiRequest) request.getAttribute(API_REQUEST_KEY);
    }

    public ApiRequest getOrCreateApiRequest(ServletRequest servletRequest) throws IOException
    {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        ApiRequest apiRequest = getApiRequest(request);

        if (apiRequest == null) {

            String key = servletRequest.getParameter("key");
            String forwardedForIp = request.getHeader("x-forwarded-for");
            String remoteIp = forwardedForIp == null ? request.getRemoteAddr() : forwardedForIp;

            String uri = request.getRequestURI();

            /** Check that the url is formatted correctly */
            if (validateRequest(uri, remoteIp, request)) {
                /** The validate request method actually creates the request upon validation. We then access it here */
                if (authenticateUser(key, remoteIp, uri, request)) {
                    apiRequest = getApiRequest(request);
                }
            }


        }
        return apiRequest;
    }

    /**
     * Simply sets the given response object as an attribute within the request. This is used
     * for passing response data to the formatting methods for output processing.
     * @param response  Object containing response data
     * @param request   ServletRequest
     */
    public static void setApiResponse(Object response, ServletRequest request)
    {
        request.setAttribute(RESPONSE_OBJECT_KEY, response);
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

        logger.trace("Serializing response as " + format);

        Object responseObj = request.getAttribute(RESPONSE_OBJECT_KEY);

        /** Set a response error if the response object attribute is not set */
        if (responseObj == null) {
            responseObj = new ApiError(RESPONSE_ERROR);
        }

        try {
            String responseStr;
            if (format.equalsIgnoreCase(FormatType.XML.name())) {
                responseStr = xmlMapper.writeValueAsString(responseObj);
                response.setContentType("application/xml");
            }
            else if (format.equalsIgnoreCase(FormatType.JSONP.name())) {
                String callback = request.getParameter("callback");
                String json = jsonMapper.writeValueAsString(responseObj);
                responseStr = String.format("%s(%s);", callback, json);
                response.setContentType("application/javascript");
            }
            else {
                responseStr = jsonMapper.writeValueAsString(responseObj);
                response.setContentType("application/json");
            }
            request.setAttribute(FORMATTED_RESPONSE_KEY, responseStr);
            response.setCharacterEncoding("UTF-8");
            response.setContentLength(responseStr.getBytes("UTF-8").length);
            logger.trace("Completed serialization");
        }
        catch (JsonProcessingException ex) {
            logger.error(fatal, "Failed to serialize response!", ex);
            request.setAttribute(FORMATTED_RESPONSE_KEY, RESPONSE_SERIALIZATION_ERROR);
        }
        catch (UnsupportedEncodingException ex) {
            logger.error("Failed to get UTF-8 bytes from response!", ex);
            request.setAttribute(FORMATTED_RESPONSE_KEY, RESPONSE_SERIALIZATION_ERROR);
        }
    }

    /**
     * Writes the formatted response to the output stream.
     * @param request   ServletRequest
     * @param response  ServletResponse
     */
    private void sendResponse(ServletRequest request, ServletResponse response)
    {
        Object formattedResponse = request.getAttribute(FORMATTED_RESPONSE_KEY);
        logger.trace("Writing Api response...");
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