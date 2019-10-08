package gov.nysenate.sage.controller.admin;

import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.client.response.base.GenericResponse;
import gov.nysenate.sage.dao.model.admin.SqlAdminUserDao;
import gov.nysenate.sage.service.data.DataGenService;
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
import static gov.nysenate.sage.util.controller.ApiControllerUtil.*;

@Controller
@RequestMapping(value = ConstantUtil.ADMIN_REST_PATH + "/datagen")
public class DataGenController {

    private Logger logger = LoggerFactory.getLogger(DataGenController.class);
    private SqlAdminUserDao sqlAdminUserDao;
    private AdminUserAuth adminUserAuth;
    private DataGenService dataGenService;

    @Autowired
    public DataGenController(SqlAdminUserDao sqlAdminUserDao, AdminUserAuth adminUserAuth,
                             DataGenService dataGenService) {
        this.sqlAdminUserDao = sqlAdminUserDao;
        this.adminUserAuth = adminUserAuth;
        this.dataGenService = dataGenService;
    }

    /**
     * Generate Meta Data Api
     * -----------------------
     *
     * Removes invalid states from the geocache
     *
     * Usage:
     * (GET)    /admin/datagen/genmetadata/{option}
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param option String value that can be either all, assembly, congress, senate, a, c, s
     * @param username String
     * @param password String
     *
     */
    @RequestMapping(value = "/genmetadata/{option}", method = RequestMethod.GET)
    public void generateMetaData(HttpServletRequest request, HttpServletResponse response,
                                 @PathVariable String option,
                                 @RequestParam(required = false, defaultValue = "defaultUser") String username,
                                 @RequestParam(required = false, defaultValue = "defaultPass") String password) {
        Object apiResponse;
        String ipAddr= ApiControllerUtil.getIpAddress(request);
        Subject subject = SecurityUtils.getSubject();

        boolean validCredentialInput = adminUserAuth.isUserNamePasswordValidInput(username, password);

        if (subject.hasRole("ADMIN") || ( validCredentialInput && sqlAdminUserDao.checkAdminUser(username, password)) ) {
            adminUserAuth.setUpPermissions(request, username, ipAddr);
            try {
                apiResponse = dataGenService.generateMetaData(option);
            }
            catch (Exception e) {
                apiResponse = new ApiError(this.getClass(), INTERNAL_ERROR);
            }
        }
        else {
            apiResponse = invalidAuthResponse();
        }
        setAdminResponse(apiResponse, response);
    }

    /**
     * Generate County Code File Api
     * -----------------------------
     *
     * Creates a county code file for use with the Street File parsing
     *
     * Usage:
     * (GET)    /admin/datagen/countycodes
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     *
     */
    @RequestMapping(value = "/countycodes", method = RequestMethod.GET)
    public void ensureCountyCodeFileExists(HttpServletRequest request, HttpServletResponse response) {
        Object apiResponse = new ApiError(this.getClass(), INTERNAL_ERROR);
        if (dataGenService.ensureCountyCodeFile()) {
            apiResponse = new GenericResponse(true,  SUCCESS.getCode() + ": " + SUCCESS.getDesc());
        }
        setAdminResponse(apiResponse, response);
    }

    /**
     * Generate Town Code File Api
     * ---------------------------
     *
     * Creates a town code file for use with the Street File parsing
     *
     * Usage:
     * (GET)    /admin/datagen/towncodes
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     *
     */
    @RequestMapping(value = "/towncodes", method = RequestMethod.GET)
    public void ensureTownCodeFileExists(HttpServletRequest request, HttpServletResponse response) {
        Object apiResponse = new ApiError(this.getClass(), INTERNAL_ERROR);
        if (dataGenService.ensureTownCodeFile()) {
            apiResponse = new GenericResponse(true,  SUCCESS.getCode() + ": " + SUCCESS.getDesc());
        }
        setAdminResponse(apiResponse, response);
    }

    /**
     * Generate Zip Code CSV File Api [test case: scope public]
     * ---------------------------
     *
     * Creates a zip code csv file for use with the Street File parsing
     *
     * Usage:
     * (GET)    /admin/datagen/zipcodes
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     *
     */

    @RequestMapping(value = "/zipcodes", method = RequestMethod.GET)
    public void generateZipCodeFiles(HttpServletRequest request, HttpServletResponse response,
                                 @RequestParam(required = false, defaultValue = "defaultUser") String username,
                                 @RequestParam(required = false, defaultValue = "defaultPass") String password) {
        Object apiResponse;
        String ipAddr= ApiControllerUtil.getIpAddress(request);
        Subject subject = SecurityUtils.getSubject();

        boolean validCredentialInput = adminUserAuth.isUserNamePasswordValidInput(username, password);

        if (subject.hasRole("ADMIN") || ( validCredentialInput && sqlAdminUserDao.checkAdminUser(username, password)) ) {
            adminUserAuth.setUpPermissions(request, username, ipAddr);
            try {
                apiResponse = dataGenService.generateZipCsv();
            }
            catch (Exception e) {
                apiResponse = new ApiError(this.getClass(), INTERNAL_ERROR);
            }
        }
        else {
            apiResponse = invalidAuthResponse();
        }

        setAdminResponse(apiResponse, response);
    }
}
