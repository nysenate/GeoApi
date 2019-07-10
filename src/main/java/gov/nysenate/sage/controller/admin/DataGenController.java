package gov.nysenate.sage.controller.admin;

import gov.nysenate.sage.client.response.base.ApiError;
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
@RequestMapping(value = ConstantUtil.ADMIN_REST_PATH + "datagen")
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
     * REQUIRES ADMIN PERMISSIONS
     * @param request
     * @param response
     * @param path
     * @param height
     * @param username
     * @param password
     */
    @RequestMapping(value = "/gensenatorimages/", method = RequestMethod.GET)
    public void generateSenatorImages(HttpServletRequest request, HttpServletResponse response,
                                      @RequestParam String path, @RequestParam int height,
                                      @RequestParam String username,
                                      @RequestParam(required = false) String password) {
        Object apiResponse;
        String ipAddr= ApiControllerUtil.getIpAddress(request);
        Subject subject = SecurityUtils.getSubject();

        if (subject.hasRole("ADMIN") || sqlAdminUserDao.checkAdminUser(username, password)) {
            adminUserAuth.setUpPermissions(request, username, ipAddr);
            apiResponse = dataGenService.generateSenatorImages(path, height);
        }
        else {
            apiResponse = invalidAuthResponse();
        }
        setAdminResponse(apiResponse, response);
    }

    /**
     * REQUIRES ADMIN PERMISSIONS
     * Generate Assembly, Congressional, and Senate meta data
     * @param request
     * @param response
     * @param option String value that can be either all, assembly, congress, senate, a, c, s
     * @param username
     * @param password
     */
    @RequestMapping(value = "/genmetadata/{option}", method = RequestMethod.GET)
    public void generateMetaData(HttpServletRequest request, HttpServletResponse response,
                                 @PathVariable String option, @RequestParam String username,
                                 @RequestParam(required = false) String password) {
        Object apiResponse;
        String ipAddr= ApiControllerUtil.getIpAddress(request);
        Subject subject = SecurityUtils.getSubject();

        if (subject.hasRole("ADMIN") || sqlAdminUserDao.checkAdminUser(username, password)) {
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

    @RequestMapping(value = "/countycodes", method = RequestMethod.GET)
    public void ensureCountyCodeFileExists(HttpServletRequest request, HttpServletResponse response) {
        Object apiResponse = new ApiError(this.getClass(), INTERNAL_ERROR);
        if (dataGenService.ensureCountyCodeFile()) {
            apiResponse = new ApiError(this.getClass(), SUCCESS);
        }
        setAdminResponse(apiResponse, response);
    }

    @RequestMapping(value = "/towncodes", method = RequestMethod.GET)
    public void ensureTownCodeFileExists(HttpServletRequest request, HttpServletResponse response) {
        Object apiResponse = new ApiError(this.getClass(), INTERNAL_ERROR);
        if (dataGenService.ensureTownCodeFile()) {
            apiResponse = new ApiError(this.getClass(), SUCCESS);
        }
        setAdminResponse(apiResponse, response);
    }
}
