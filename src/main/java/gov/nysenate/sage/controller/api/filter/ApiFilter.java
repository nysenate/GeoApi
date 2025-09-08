package gov.nysenate.sage.controller.api.filter;

import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.dao.logger.apirequest.SqlApiRequestLogger;
import gov.nysenate.sage.dao.model.api.ApiUserDao;
import gov.nysenate.sage.dao.model.api.RequiredApiUser;
import gov.nysenate.sage.model.api.ApiRequest;
import gov.nysenate.sage.model.api.ApiUser;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.util.FormatUtil;
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
    private final ApiUserDao apiUserDao;
    private final String ipFilter;

    /** Api services that are designated as public */
    @Value("${public.api.filter:(map)}")
    private String publicApiFilter;
    @Value("${api.logging.enabled:true}")
    private boolean apiLoggingEnabled;

    @Autowired
    public ApiFilter(Environment env, SqlApiRequestLogger sqlApiRequestLogger, ApiUserDao apiUserDao) {
        this.sqlApiRequestLogger = sqlApiRequestLogger;
        this.apiUserDao = apiUserDao;
        this.ipFilter = env.getUserIpFilter();
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse response,
                         FilterChain filterChain) throws IOException, ServletException {
        var request = (HttpServletRequest) servletRequest;
        var apiRequest = new ApiRequest(request);
        String key = servletRequest.getParameter("key");
        ApiUser apiUser;
        if (key == null) {
            if (apiRequest.getHostAddress().matches(ipFilter)) {
                apiUser = apiUserDao.getRequiredApiUser(RequiredApiUser.DEFAULT);
            }
            else if (apiRequest.getService() != null && apiRequest.getService().matches(publicApiFilter)) {
                apiUser = apiUserDao.getRequiredApiUser(RequiredApiUser.PUBLIC);
            }
            else {
                writeErrorResponse(API_KEY_MISSING, response);
                return;
            }
        }
        else {
            apiUser = apiUserDao.getApiUserByKey(key);
        }
        apiRequest.setApiUser(apiUser);

        // The filter will proceed to the next chain only if the user has a valid key or is the default user.
        // Otherwise, an error message will be sent.
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
