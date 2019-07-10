package gov.nysenate.sage.controller.admin;

import gov.nysenate.sage.dao.model.admin.SqlAdminUserDao;
import gov.nysenate.sage.service.data.DataDelService;
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

import static gov.nysenate.sage.util.controller.ApiControllerUtil.invalidAuthResponse;
import static gov.nysenate.sage.util.controller.ApiControllerUtil.setAdminResponse;

@Controller
@RequestMapping(value = ConstantUtil.ADMIN_REST_PATH + "data/delete")
public class DataDelController {

    private Logger logger = LoggerFactory.getLogger(DataGenController.class);
    private SqlAdminUserDao sqlAdminUserDao;
    private AdminUserAuth adminUserAuth;
    private DataDelService dataDelService;

    @Autowired
    public DataDelController(SqlAdminUserDao sqlAdminUserDao, AdminUserAuth adminUserAuth, DataDelService dataDelService) {
        this.sqlAdminUserDao = sqlAdminUserDao;
        this.adminUserAuth = adminUserAuth;
        this.dataDelService = dataDelService;
    }

    @RequestMapping(value = "/zips/{offset}", method = RequestMethod.GET)
    public void cleanUpBadZipsInGeocache(HttpServletRequest request, HttpServletResponse response,
                                      @RequestParam String username,
                                      @RequestParam(required = false) String password,
                                        @PathVariable Integer offset) {
        Object apiResponse;
        String ipAddr= ApiControllerUtil.getIpAddress(request);
        Subject subject = SecurityUtils.getSubject();

        if (subject.hasRole("ADMIN") || sqlAdminUserDao.checkAdminUser(username, password)) {
            adminUserAuth.setUpPermissions(request, username, ipAddr);

            apiResponse = dataDelService.cleanUpBadZips(offset);
        }
        else {
            apiResponse = invalidAuthResponse();
        }
        setAdminResponse(apiResponse, response);
    }

    @RequestMapping(value = "/states", method = RequestMethod.GET)
    public void cleanUpBadStatesInGeocache(HttpServletRequest request, HttpServletResponse response,
                                      @RequestParam String username,
                                      @RequestParam(required = false) String password) {
        Object apiResponse;
        String ipAddr= ApiControllerUtil.getIpAddress(request);
        Subject subject = SecurityUtils.getSubject();

        if (subject.hasRole("ADMIN") || sqlAdminUserDao.checkAdminUser(username, password)) {
            adminUserAuth.setUpPermissions(request, username, ipAddr);
            apiResponse = dataDelService.cleanUpBadStates();
        }
        else {
            apiResponse = invalidAuthResponse();
        }
        setAdminResponse(apiResponse, response);
    }

}
