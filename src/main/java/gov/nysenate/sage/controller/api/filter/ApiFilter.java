package gov.nysenate.sage.controller.api.filter;

import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.dao.logger.apirequest.SqlApiRequestLogger;
import gov.nysenate.sage.model.api.ApiRequest;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.util.FormatUtil;
import gov.nysenate.sage.util.auth.ApiUserAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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

    private final SqlApiRequestLogger sqlApiRequestLogger;
    private final ApiUserAuth apiUserAuth;
    private final String ipFilter;

    /** Api services that are designated as public */
    @Value("${public.api.filter:(map)}")
    private String publicApiFilter;
    @Value("${user.public.key}")
    private String publicKey;
    @Value("${user.default.key}")
    private String defaultKey;
    @Value("${api.logging.enabled:true}")
    private boolean apiLoggingEnabled;

    /** Available format types */
    public enum FormatType { JSON, XML }

    @Autowired
    public ApiFilter(Environment env, SqlApiRequestLogger sqlApiRequestLogger, ApiUserAuth apiUserAuth) {
        this.sqlApiRequestLogger = sqlApiRequestLogger;
        this.apiUserAuth = apiUserAuth;
        this.ipFilter = env.getUserIpFilter();
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse response,
                         FilterChain filterChain) throws IOException, ServletException {
        var request = (HttpServletRequest) servletRequest;
        var apiRequest = new ApiRequest(request);
        String key = servletRequest.getParameter("key");
        if (key == null) {
            if (apiRequest.getHostAddress().matches(ipFilter)) {
                key = defaultKey;
            }
            else if (apiRequest.getService() != null && apiRequest.getService().matches(publicApiFilter)) {
                key = publicKey;
            }
            else {
                writeErrorResponse(API_KEY_MISSING, response);
                return;
            }
        }
        apiRequest.setApiUser(apiUserAuth.getApiUser(key));

        // Check that the url is formatted correctly
        if (validateRequest(request)) {
            // The filter will proceed to the next chain only if the user has a valid key or is the default user.
            // Otherwise, an error message will be sent. */
            if (apiRequest.getApiUser() != null) {
                if (apiRequest.isValid()) {
                    if (apiLoggingEnabled) {
                        sqlApiRequestLogger.logApiRequest(apiRequest);
                    }
                    filterChain.doFilter(request, response);
                }
                else {
                    writeErrorResponse(API_REQUEST_INVALID, response);
                }
            }
            else {
                writeErrorResponse(API_KEY_INVALID, response);
                logger.warn("Failed to validate request using key: {}", key);
            }
        }
        else {
            writeErrorResponse(API_OUTPUT_FORMAT_UNSUPPORTED, response);
        }
    }

    /**
     * Parses the URI to obtain the API attributes. Sets them as request attributes so they
     * can be accessed by the controllers. Also checks to see if the output format is valid.
     * @return  true if api parsed correctly
     *          false otherwise
     */
    // TODO: ensure XML outputs properly for Bluebird
    private boolean validateRequest(ServletRequest request) {
        // Validate the output format only if it is set. It is okay if the format is not specified.
        FormatType formatType = FormatType.JSON;
        try {
            FormatType format = FormatType.valueOf(request.getParameter("format").toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return false;
        }
        catch (NullPointerException ignored) {}
        return true;
    }

    /**
     * Write an error json response
     * @param response HttpServletResponse
     * @throws IOException if it failed to write or flush buffer.
     */
    private void writeErrorResponse(ResultStatus errorStatus, ServletResponse response) throws IOException {
        var errorResponse = new ApiError(errorStatus);
        response.getWriter().append(FormatUtil.toJsonString(errorResponse));
        response.setContentType("application/json");
        ((HttpServletResponse) response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.flushBuffer();
    }
}
