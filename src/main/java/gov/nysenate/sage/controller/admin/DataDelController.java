package gov.nysenate.sage.controller.admin;

import gov.nysenate.sage.dao.model.admin.SqlAdminUserDao;
import gov.nysenate.sage.service.data.DataDelService;
import gov.nysenate.sage.util.auth.AdminUserAuth;
import gov.nysenate.sage.util.auth.ApiUserAuth;
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

import static gov.nysenate.sage.util.controller.ApiControllerUtil.invalidAuthResponse;
import static gov.nysenate.sage.util.controller.ApiControllerUtil.setAdminResponse;

@Controller
@RequestMapping(value = ConstantUtil.ADMIN_REST_PATH + "/datadel")
public class DataDelController {

    private Logger logger = LoggerFactory.getLogger(DataGenController.class);
    private AdminUserAuth adminUserAuth;
    private ApiUserAuth apiUserAuth;
    private DataDelService dataDelService;

    @Autowired
    public DataDelController(AdminUserAuth adminUserAuth,
                             ApiUserAuth apiUserAuth, DataDelService dataDelService) {
        this.adminUserAuth = adminUserAuth;
        this.apiUserAuth = apiUserAuth;
        this.dataDelService = dataDelService;
    }


    /**
     * Remove Bad Zips Api
     * ---------------------
     *
     * Removes non integer zip codes from the geocache
     *
     * Usage:
     * (GET)    /admin/datadel/zips/{offset}
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param username String
     * @param password String
     * @param offset Integer
     *
     */
    @RequestMapping(value = "/zips/{offset}", method = RequestMethod.GET)
    public void cleanUpBadZipsInGeocache(HttpServletRequest request, HttpServletResponse response,
                                      @RequestParam(required = false, defaultValue = "defaultUser") String username,
                                      @RequestParam(required = false, defaultValue = "defaultPass") String password,
                                         @RequestParam(required = false, defaultValue = "") String key,
                                        @PathVariable Integer offset) {
        Object apiResponse;
        String ipAddr= ApiControllerUtil.getIpAddress(request);
        Subject subject = SecurityUtils.getSubject();

        if (subject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request,username, password, subject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, subject, ipAddr, key)) {

            apiResponse = dataDelService.cleanUpBadZips(offset);
        }
        else {
            apiResponse = invalidAuthResponse();
        }
        setAdminResponse(apiResponse, response);
    }

    /**
     * Remove Bad States Api
     * ---------------------
     *
     * Removes invalid states from the geocache
     *
     * Usage:
     * (GET)    /admin/datadel/states
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param username String
     * @param password String
     *
     */
    @RequestMapping(value = "/states", method = RequestMethod.GET)
    public void cleanUpBadStatesInGeocache(HttpServletRequest request, HttpServletResponse response,
                                      @RequestParam(required = false, defaultValue = "defaultUser") String username,
                                      @RequestParam(required = false, defaultValue = "defaultPass") String password,
                                           @RequestParam(required = false, defaultValue = "") String key) {
        Object apiResponse;
        String ipAddr= ApiControllerUtil.getIpAddress(request);
        Subject subject = SecurityUtils.getSubject();

        if (subject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request,username, password, subject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, subject, ipAddr, key) ) {
            apiResponse = dataDelService.cleanUpBadStates();
        }
        else {
            apiResponse = invalidAuthResponse();
        }
        setAdminResponse(apiResponse, response);
    }

}
