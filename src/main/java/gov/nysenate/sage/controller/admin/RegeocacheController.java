package gov.nysenate.sage.controller.admin;

import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.service.data.RegeocacheService;
import gov.nysenate.sage.util.auth.AdminUserAuth;
import gov.nysenate.sage.util.auth.ApiUserAuth;
import gov.nysenate.sage.util.controller.ApiControllerUtil;
import gov.nysenate.sage.util.controller.ConstantUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;

import static gov.nysenate.sage.model.result.ResultStatus.API_REQUEST_INVALID;
import static gov.nysenate.sage.util.controller.ApiControllerUtil.setApiResponse;

@Controller
@RequestMapping(value = ConstantUtil.ADMIN_REST_PATH + "/regeocache")
public class RegeocacheController {
    private final AdminUserAuth adminUserAuth;
    private final ApiUserAuth apiUserAuth;
    private final RegeocacheService regeocacheService;

    @Autowired
    public RegeocacheController(AdminUserAuth adminUserAuth, ApiUserAuth apiUserAuth,
                                RegeocacheService regeocacheService) {
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
    public void geocacheZips(HttpServletRequest request, HttpServletResponse response,
                             @RequestParam(required = false, defaultValue = "defaultUser") String username,
                             @RequestParam(required = false, defaultValue = "defaultPass") String password,
                             @RequestParam(required = false, defaultValue = "") String key) {
        Object apiResponse = new ApiError(this.getClass(), API_REQUEST_INVALID);
        String ipAddr = ApiControllerUtil.getIpAddress(request);
        Subject subject = SecurityUtils.getSubject();

        if (subject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request, username, password, subject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, subject, ipAddr, key)) {
            apiResponse = regeocacheService.updateZipsInGeocache();
        }
        setApiResponse(apiResponse, request);
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
    public void nysRefreshGeocache(HttpServletRequest request, HttpServletResponse response,
                                   @PathVariable int offset,
                                   @RequestParam(required = false, defaultValue = "defaultUser") String username,
                                   @RequestParam(required = false, defaultValue = "defaultPass") String password,
                                   @RequestParam(required = false, defaultValue = "") String key) {

        Object apiResponse = new ApiError(this.getClass(), API_REQUEST_INVALID);
        String ipAddr = ApiControllerUtil.getIpAddress(request);
        Subject subject = SecurityUtils.getSubject();

        if (subject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request, username, password, subject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, subject, ipAddr, key)) {
            apiResponse = regeocacheService.updateGeocacheWithNYSGeoData(offset);
        }
        setApiResponse(apiResponse, request);
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
    public void nysRefreshGeocache(HttpServletRequest request, HttpServletResponse response,
                                   @RequestParam(required = false, defaultValue = "defaultUser") String username,
                                   @RequestParam(required = false, defaultValue = "defaultPass") String password,
                                   @RequestParam(required = false, defaultValue = "") String key) {

        Object apiResponse = new ApiError(this.getClass(), API_REQUEST_INVALID);
        String ipAddr = ApiControllerUtil.getIpAddress(request);
        Subject subject = SecurityUtils.getSubject();

        if (subject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request, username, password, subject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, subject, ipAddr, key)) {

            ArrayList<String> params = new ArrayList<>();
            params.add("provider");
            params.add("nysgeo");
            apiResponse = regeocacheService.massRegeoache(1, 10, false, params);
        }

        setApiResponse(apiResponse, request);
    }
}
