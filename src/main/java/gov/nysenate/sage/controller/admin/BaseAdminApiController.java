package gov.nysenate.sage.controller.admin;

import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.util.auth.AdminUserAuth;
import gov.nysenate.sage.util.auth.ApiUserAuth;
import gov.nysenate.sage.util.controller.ApiControllerUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import static gov.nysenate.sage.model.result.ResultStatus.INTERNAL_ERROR;

public abstract class BaseAdminApiController {
    private static final Logger logger = LoggerFactory.getLogger(BaseAdminApiController.class);
    private final AdminUserAuth adminUserAuth;
    private final ApiUserAuth apiUserAuth;

    protected BaseAdminApiController(AdminUserAuth adminUserAuth, ApiUserAuth apiUserAuth) {
        this.adminUserAuth = adminUserAuth;
        this.apiUserAuth = apiUserAuth;
    }


    protected boolean authenticate(HttpServletRequest request, String username, String password, String key) {
        String ipAddr = ApiControllerUtil.getIpAddress(request);
        Subject subject = SecurityUtils.getSubject();
        return subject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request, username, password, subject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, subject, ipAddr, key);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleException(Exception ex) {
        logger.error("Error during admin API call", ex);
        return new ApiError(getClass(), INTERNAL_ERROR);
    }
}
