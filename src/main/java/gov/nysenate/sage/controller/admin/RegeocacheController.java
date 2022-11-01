package gov.nysenate.sage.controller.admin;

import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.client.response.base.GenericResponse;
import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.service.data.RegeocacheService;
import gov.nysenate.sage.util.auth.AdminUserAuth;
import gov.nysenate.sage.util.auth.ApiUserAuth;
import gov.nysenate.sage.util.controller.ApiControllerUtil;
import gov.nysenate.sage.util.controller.ConstantUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.subject.WebSubject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;

import static gov.nysenate.sage.model.result.ResultStatus.*;
import static gov.nysenate.sage.util.controller.ApiControllerUtil.setAdminResponse;
import static gov.nysenate.sage.util.controller.ApiControllerUtil.setApiResponse;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = ConstantUtil.ADMIN_REST_PATH + "/regeocache", produces = APPLICATION_JSON_VALUE)
public class RegeocacheController {

    private Logger logger = LoggerFactory.getLogger(RegeocacheController.class);

    private BaseDao baseDao;
    private Environment env;
    private AdminUserAuth adminUserAuth;
    private ApiUserAuth apiUserAuth;
    private RegeocacheService regeocacheService;

    @Autowired
    @Qualifier("securityManager")
    protected DefaultWebSecurityManager securityManager;

    @Autowired
    public RegeocacheController(BaseDao baseDao, Environment env,
                                AdminUserAuth adminUserAuth, ApiUserAuth apiUserAuth,
                                RegeocacheService regeocacheService) {
        this.baseDao = baseDao;
        this.env = env;
        this.adminUserAuth = adminUserAuth;
        this.apiUserAuth = apiUserAuth;
        this.regeocacheService = regeocacheService;
    }

    /**
     * Regeocache Zips Api
     * ---------------------------
     * <p>
     * Regeocache the zip codes in the geocache
     * <p>
     * Usage:
     * (GET)    /admin/regeocache/zip
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @param username String
     * @param password String
     */
    @RequestMapping(value = "/zip", method = RequestMethod.GET)
    public Object geocacheZips(HttpServletRequest request, HttpServletResponse response,
                             @RequestParam(required = false, defaultValue = "defaultUser") String username,
                             @RequestParam(required = false, defaultValue = "defaultPass") String password,
                             @RequestParam(required = false, defaultValue = "") String key) {
        Object apiResponse = new ApiError(this.getClass(), API_REQUEST_INVALID);
        String ipAddr = ApiControllerUtil.getIpAddress(request);
        WebSubject subject = createSubject(request, response);

        if (subject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request, username, password, subject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, subject, ipAddr, key)) {
            apiResponse = regeocacheService.updateZipsInGeocache();
        }
//        setApiResponse(apiResponse, request);
        return apiResponse;
    }

    /**
     * NYS Geocache Refresh Api
     * ---------------------------
     * <p>
     * Update the geocache with NYSGEO data
     * <p>
     * Usage:
     * (GET)    /admin/regeocache/nysrefresh/{offset}
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @param username String
     * @param password String
     */
    @RequestMapping(value = "/nysrefresh/{offset}", method = RequestMethod.GET)
    public Object nysRefreshGeocache(HttpServletRequest request, HttpServletResponse response,
                                   @PathVariable int offset,
                                   @RequestParam(required = false, defaultValue = "defaultUser") String username,
                                   @RequestParam(required = false, defaultValue = "defaultPass") String password,
                                   @RequestParam(required = false, defaultValue = "") String key) {

        Object apiResponse = new ApiError(this.getClass(), API_REQUEST_INVALID);
        String ipAddr = ApiControllerUtil.getIpAddress(request);
        WebSubject subject = createSubject(request, response);

        if (subject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request, username, password, subject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, subject, ipAddr, key)) {
            apiResponse = regeocacheService.updateGeocacheWithNYSGeoData(offset);
        }
//        setApiResponse(apiResponse, request);
        return apiResponse;
    }


    /**
     * Mass Regeocache Testing
     * ---------------------------
     *
     * Test Mass Geocache CLI script, Not intended for everyday use. For debugging only
     *
     * Usage:
     * (GET)    /admin/regeocache/mass
     */
    @RequestMapping(value = "/mass", method = RequestMethod.GET)
    public Object nysRefreshGeocache(HttpServletRequest request, HttpServletResponse response,
                                   @RequestParam(required = false, defaultValue = "defaultUser") String username,
                                   @RequestParam(required = false, defaultValue = "defaultPass") String password,
                                   @RequestParam(required = false, defaultValue = "") String key) {

        Object apiResponse = new ApiError(this.getClass(), API_REQUEST_INVALID);
        String ipAddr = ApiControllerUtil.getIpAddress(request);
        WebSubject subject = createSubject(request, response);

        if (subject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request, username, password, subject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, subject, ipAddr, key)) {

            ArrayList<String> params = new ArrayList<>();
            params.add("method");
            params.add("YahooDao");
            params.add("provider");
            params.add("nysgeo");
            apiResponse = regeocacheService.massRegeoache(1, 10, false, params);
        }

//        setApiResponse(apiResponse, request);
        return apiResponse;
    }

    protected WebSubject createSubject(ServletRequest request, ServletResponse response) {
        return new WebSubject.Builder(securityManager, request, response).buildWebSubject();
    }
}
