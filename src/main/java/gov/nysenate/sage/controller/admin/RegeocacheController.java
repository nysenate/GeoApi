package gov.nysenate.sage.controller.admin;

import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.model.admin.SqlAdminUserDao;
import gov.nysenate.sage.service.data.RegeocacheService;
import gov.nysenate.sage.util.auth.AdminUserAuth;
import gov.nysenate.sage.util.controller.ApiControllerUtil;
import gov.nysenate.sage.util.controller.ConstantUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static gov.nysenate.sage.model.result.ResultStatus.*;
import static gov.nysenate.sage.util.controller.ApiControllerUtil.setApiResponse;

@Controller
@RequestMapping(value = ConstantUtil.ADMIN_REST_PATH + "/regeocache")
public class RegeocacheController {

    private Logger logger = LoggerFactory.getLogger(RegeocacheController.class);

    private BaseDao baseDao;
    private Environment env;
    private SqlAdminUserDao sqlAdminUserDao;
    private AdminUserAuth adminUserAuth;
    private RegeocacheService regeocacheService;

    @Autowired
    public RegeocacheController(BaseDao baseDao, Environment env, SqlAdminUserDao sqlAdminUserDao,
                                AdminUserAuth adminUserAuth, RegeocacheService regeocacheService) {
        this.baseDao = baseDao;
        this.env = env;
        this.sqlAdminUserDao = sqlAdminUserDao;
        this.adminUserAuth = adminUserAuth;
        this.regeocacheService = regeocacheService;
    }

    /**
     * Regeocache Zips Api
     * ---------------------------
     *
     * Generates Senator images with the specified height
     *
     * Usage:
     * (GET)    /admin/regeocache/zip
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param username String
     * @param password String
     *
     */
    @RequestMapping(value = "/zip", method = RequestMethod.GET)
    public void geocacheZips(HttpServletRequest request, HttpServletResponse response,
                             @RequestParam(required = false, defaultValue = "defaultUser") String username,
                             @RequestParam(required = false, defaultValue = "defaultPass") String password) {
        Object apiResponse = new ApiError(this.getClass(), API_REQUEST_INVALID);
        String ipAddr= ApiControllerUtil.getIpAddress(request);
        Subject subject = SecurityUtils.getSubject();

        boolean validCredentialInput = adminUserAuth.isUserNamePasswordValidInput(username, password);

        if (subject.hasRole("ADMIN") || ( validCredentialInput && sqlAdminUserDao.checkAdminUser(username, password)) ) {
            adminUserAuth.setUpPermissions(request, username, ipAddr);
            apiResponse = regeocacheService.updateZipsInGeocache();
        }
        setApiResponse(apiResponse, request);
    }

    /**
     * NYS Geocache Refresh Api
     * ---------------------------
     *
     * Generates Senator images with the specified height
     *
     * Usage:
     * (GET)    /admin/regeocache/nysrefresh/{offset}
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param username String
     * @param password String
     *
     */
    @RequestMapping(value = "/nysrefresh/{offset}", method = RequestMethod.GET)
    public void nysRefreshGeocache(HttpServletRequest request, HttpServletResponse response,
                                   @PathVariable int offset,
                                   @RequestParam(required = false, defaultValue = "defaultUser") String username,
                                   @RequestParam(required = false, defaultValue = "defaultPass") String password) {

        Object apiResponse = new ApiError(this.getClass(), API_REQUEST_INVALID);
        String ipAddr= ApiControllerUtil.getIpAddress(request);
        Subject subject = SecurityUtils.getSubject();

        boolean validCredentialInput = adminUserAuth.isUserNamePasswordValidInput(username, password);

        if (subject.hasRole("ADMIN") || ( validCredentialInput && sqlAdminUserDao.checkAdminUser(username, password)) ) {
            adminUserAuth.setUpPermissions(request, username, ipAddr);
            apiResponse = regeocacheService.updateGeocacheWithNYSGeoData(offset);
        }
        setApiResponse(apiResponse, request);
    }


    /**
     * NYS Geocache Dups Refresh Api
     * ---------------------------
     *
     * Generates Senator images with the specified height
     *
     * Usage:
     * (GET)    /admin/regeocache/nysrefresh/dups/{offset}
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param username String
     * @param password String
     *
     */
    @RequestMapping(value = "/nysrefresh/dups/{offset}", method = RequestMethod.GET)
    public void handleNysDupsInGeocache(HttpServletRequest request, HttpServletResponse response,
                                   @PathVariable int offset,
                                        @RequestParam(required = false, defaultValue = "defaultUser") String username,
                                   @RequestParam(required = false, defaultValue = "defaultPass") String password) {

        Object apiResponse = new ApiError(this.getClass(), API_REQUEST_INVALID);
        String ipAddr= ApiControllerUtil.getIpAddress(request);
        Subject subject = SecurityUtils.getSubject();

        boolean validCredentialInput = adminUserAuth.isUserNamePasswordValidInput(username, password);

        if (subject.hasRole("ADMIN") || ( validCredentialInput && sqlAdminUserDao.checkAdminUser(username, password)) ) {
            adminUserAuth.setUpPermissions(request, username, ipAddr);
            apiResponse = regeocacheService.updatesDupsInGeocacheWithNysGeo(offset);
        }
        setApiResponse(apiResponse, request);
    }
}
