package gov.nysenate.sage.controller.api.filter;

import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.dao.logger.apirequest.SqlApiRequestLogger;
import gov.nysenate.sage.model.api.ApiRequest;
import gov.nysenate.sage.model.api.ApiUser;
import gov.nysenate.sage.util.auth.ApiUserAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
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
 * This filter processes both the request propagating to the controllers and the response coming back from the controllers.
 *
 * @author Ash Islam
 */
@Component
public class ApiFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(ApiFilter.class);

    /** String keys used for setting key value attributes in the request object */
    private static final String RESPONSE_OBJECT_KEY = "responseObject";
    private static final String API_REQUEST_KEY = "apiRequest";
    /** The valid format of an api request */
    private static final String validFormat = "api/v\\d+/(?<service>\\w+)/(?<request>\\w+)";

    private final SqlApiRequestLogger sqlApiRequestLogger;
    private final ApiUserAuth apiUserAuth;
    private final String ipFilter;
    private final String defaultKey;

    /** Api services that are designated as public */
    @Value("${public.api.filter:(map)}")
    private String publicApiFilter;
    @Value("${user.public.key}")
    private String publicKey;
    @Value("${api.logging.enabled:true}")
    private boolean apiLoggingEnabled;

    /** Available format types */
    public enum FormatType { JSON, XML, JSONP }

    @Autowired
    public ApiFilter(Environment env, SqlApiRequestLogger sqlApiRequestLogger, ApiUserAuth apiUserAuth) {
        this.sqlApiRequestLogger = sqlApiRequestLogger;
        this.apiUserAuth = apiUserAuth;
        ipFilter = env.getUserIpFilter();
        defaultKey = env.getUserDefaultKey();
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse response,
                         FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;

        String key = servletRequest.getParameter("key");
        String forwardedForIp = request.getHeader("x-forwarded-for");
        String remoteIp = forwardedForIp == null ? request.getRemoteAddr() : forwardedForIp;

        String uri = request.getRequestURI();

        // Check that the url is formatted correctly
        if (validateRequest(uri, remoteIp, request)) {
            // The filter will proceed to the next chain only if the user has a valid key or is the default user.
            // Otherwise, an error message will be sent. */
            if (authenticateUser(key, remoteIp, uri, request)) {
                filterChain.doFilter(request, response);
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
     * @param request   ServletRequest
     * @return          true if authenticated, false otherwise
     */
    private boolean authenticateUser(String key, String remoteIp, String uri, ServletRequest request) {
        ApiRequest apiRequest = (ApiRequest) request.getAttribute(API_REQUEST_KEY);
        String service = apiRequest.getService();

        if (key == null) {
            if (remoteIp.matches(ipFilter)) {
                key = defaultKey;
                logger.trace("Default user: {} granted default key {}", remoteIp, key);
            }
            else if (service.matches(publicApiFilter)) {
                key = publicKey;
                logger.trace("User: {} granted default map key for accessing public api.", remoteIp);
            }
        }

        if (key != null) {
            ApiUser apiUser = apiUserAuth.getApiUser(key);

            if (apiUser != null) {
                logger.trace("ApiUser {} has been authenticated successfully", apiUser.getName());
                apiRequest.setApiUser(apiUser);

                // Log Api Request into the database
                int id = -1;
                if (apiLoggingEnabled) {
                    id = sqlApiRequestLogger.logApiRequest(apiRequest);
                    apiRequest.setId(id);
                }

                // Cache the current Api Request ID
                request.setAttribute("apiRequestId", id);
                return true;
            }
            else {
                setApiResponse(new ApiError(API_KEY_INVALID), request);
                logger.warn("Failed to validate request to {} from {} using key: {}", uri, remoteIp, key);
            }
        }
        else {
            setApiResponse(new ApiError(API_KEY_MISSING), request);
            logger.warn("No key supplied to access {} from {}", uri, remoteIp);
        }

        return false;
    }

    /**
     * Parses the URI to obtain the API attributes. Sets them as request attributes so they
     * can be accessed by the controllers. Also checks to see if the output format is valid.
     * @return  true if api parsed correctly
     *          false otherwise
     */
    private boolean validateRequest(String uri, String remoteIp, ServletRequest request) {
        Pattern validFormatPattern = Pattern.compile(validFormat);
        Matcher matcher = validFormatPattern.matcher(uri);

        // Validate the output format only if it is set. It is okay if the format is not specified.
        String outputFormat = request.getParameter("format");
        if (outputFormat != null) {
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
            // Check for callback signature if format is JSONP
            if (outputFormat.equalsIgnoreCase(FormatType.JSONP.name()) && request.getParameter("callback") == null) {
                setApiResponse(new ApiError(JSONP_CALLBACK_NOT_SPECIFIED), request);
                return false;
            }
        }

        // If the url pattern matches, then obtain the parameters and propagate an ApiResult object as an
        // attribute with the key 'apiRequest'.
        if (matcher.find()) {
            String service = matcher.group("service");
            String requestStr = matcher.group("request");

            // Resolve IP address into InetAddress
            InetAddress remoteInetAddress = null;
            try {
                remoteInetAddress = InetAddress.getByName(remoteIp);
                logger.debug("Request from {}", remoteInetAddress.getCanonicalHostName());
            }
            catch (UnknownHostException ex) {
                logger.warn("Unknown remote ip host!", ex);
            }

            ApiRequest apiRequest = new ApiRequest(service, requestStr, remoteInetAddress);
            apiRequest.setProvider(request.getParameter("provider"));

            request.setAttribute(API_REQUEST_KEY, apiRequest);
            return true;
        }
        else {
            setApiResponse(new ApiError(API_REQUEST_INVALID), request);
            return false;
        }
    }

    /**
     * Simply sets the given response object as an attribute within the request. This is used
     * for passing response data to the formatting methods for output processing.
     * @param response  Object containing response data
     * @param request   ServletRequest
     */
    public static void setApiResponse(Object response, ServletRequest request) {
        request.setAttribute(RESPONSE_OBJECT_KEY, response);
    }
}
