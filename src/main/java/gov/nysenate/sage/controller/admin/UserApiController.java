package gov.nysenate.sage.controller.admin;

import gov.nysenate.sage.client.response.base.GenericResponse;
import gov.nysenate.sage.dao.model.api.ApiUserDao;
import gov.nysenate.sage.dao.model.job.SqlJobUserDao;
import gov.nysenate.sage.dao.stats.api.SqlApiUserStatsDao;
import gov.nysenate.sage.model.api.ApiUser;
import gov.nysenate.sage.model.job.JobUser;
import gov.nysenate.sage.util.auth.AdminUserAuth;
import gov.nysenate.sage.util.auth.ApiUserAuth;
import gov.nysenate.sage.util.auth.JobUserAuth;
import gov.nysenate.sage.util.controller.ApiControllerUtil;
import gov.nysenate.sage.util.controller.ConstantUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import static gov.nysenate.sage.util.controller.ApiControllerUtil.*;

@RestController
@RequestMapping(value = ConstantUtil.ADMIN_REST_PATH + "/api")
public class UserApiController {
    private final SqlApiUserStatsDao sqlApiUserStatsDao;
    private final ApiUserDao apiUserDao;
    private final SqlJobUserDao sqlJobUserDao;
    private final ApiUserAuth apiUserAuth;
    private final JobUserAuth jobUserAuth;
    private final AdminUserAuth adminUserAuth;

    @Autowired
    public UserApiController(SqlApiUserStatsDao sqlApiUserStatsDao,
                              ApiUserDao apiUserDao, SqlJobUserDao sqlJobUserDao,
                              ApiUserAuth apiUserAuth, JobUserAuth jobUserAuth, AdminUserAuth adminUserAuth) {
        this.sqlApiUserStatsDao = sqlApiUserStatsDao;
        this.apiUserDao = apiUserDao;
        this.sqlJobUserDao = sqlJobUserDao;
        this.apiUserAuth = apiUserAuth;
        this.jobUserAuth = jobUserAuth;
        this.adminUserAuth = adminUserAuth;
    }

    /**
     * Current Job Users Api
     * ---------------------
     * Returns the current job users
     * Usage:
     * (GET)    /admin/api/currentJobUsers
     *
     */
    @GetMapping(value = "/currentJobUsers")
    public Object currentJobUsers(HttpServletRequest request,
                                  @RequestParam(required = false, defaultValue = "defaultUser") String username,
                                  @RequestParam(required = false, defaultValue = "defaultPass") String password,
                                  @RequestParam(required = false, defaultValue = "") String key) {
        String ipAddr = ApiControllerUtil.getIpAddress(request);
        Subject subject = SecurityUtils.getSubject();
        if (subject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request,username, password, subject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, subject, ipAddr, key) ) {
            return sqlJobUserDao.getJobUsers();
        }
        return invalidAuthResponse;

    }

    /**
     * Api User Usage Api
     * ---------------------
     * Returns api user request stats
     * Usage:
     * (GET)    /admin/api/apiUserUsage
     *
     */
    @GetMapping(value = "/apiUserUsage")
    public Object apiUserUsage(HttpServletRequest request,
                               @RequestParam(required = false, defaultValue = "defaultUser") String username,
                               @RequestParam(required = false, defaultValue = "defaultPass") String password,
                               @RequestParam(required = false, defaultValue = "") String key) {
        String ipAddr = ApiControllerUtil.getIpAddress(request);
        Subject subject = SecurityUtils.getSubject();
        if (subject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request, username, password, subject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, subject, ipAddr, key)) {
            return sqlApiUserStatsDao.getRequestCounts(getBeginTimestamp(request), getEndTimestamp(request));
        }
        return invalidAuthResponse;
    }

    /**
     * Create Api Users Api
     * ---------------------
     * Creates an api user
     * Usage:
     * (POST)    /admin/api/createApiUser
     *
     */
    @PostMapping(value = "/createApiUser")
    public Object createApiUser(HttpServletRequest request,
                                @RequestParam(required = false, defaultValue = "defaultUser") String username,
                                @RequestParam(required = false, defaultValue = "defaultPass") String password,
                                @RequestParam(required = false, defaultValue = "") String key) {
        String ipAddr = ApiControllerUtil.getIpAddress(request);
        Subject subject = SecurityUtils.getSubject();
        if (subject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request,username, password, subject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, subject, ipAddr, key) ) {
            return createApiUser(request);
        }
        return invalidAuthResponse;
    }

    /**
     * Delete Api Users Api
     * ---------------------
     * Deletes an api user
     * Usage:
     * (POST)    /admin/api/deleteApiUser
     *
     */
    @PostMapping(value = "/deleteApiUser")
    public Object deleteApiUser(HttpServletRequest request,
                                @RequestParam(required = false, defaultValue = "defaultUser") String username,
                                @RequestParam(required = false, defaultValue = "defaultPass") String password,
                                @RequestParam(required = false, defaultValue = "") String key) {
        String ipAddr = ApiControllerUtil.getIpAddress(request);
        Subject subject = SecurityUtils.getSubject();
        if (subject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request,username, password, subject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, subject, ipAddr, key) ) {
            return deleteApiUser(request);
        }
        return invalidAuthResponse;
    }

    /**
     * Create Job Users Api
     * ---------------------
     * Creates a job user
     * Usage:
     * (POST)    /admin/api/createJobUser
     *
     */
    @PostMapping(value = "/createJobUser")
    public Object createJobUser(HttpServletRequest request,
                                @RequestParam(required = false, defaultValue = "defaultUser") String username,
                                @RequestParam(required = false, defaultValue = "defaultPass") String password,
                                @RequestParam(required = false, defaultValue = "") String key) {
        String ipAddr = ApiControllerUtil.getIpAddress(request);
        Subject subject = SecurityUtils.getSubject();
        if (subject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request,username, password, subject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, subject, ipAddr, key)) {
            return createJobUser(request);
        }
        return invalidAuthResponse;
    }

    /**
     * Delete Job Users Api
     * ---------------------
     * Deletes a job user
     * Usage:
     * (POST)    /admin/api/deleteJobUser
     *
     */
    @PostMapping(value = "/deleteJobUser")
    public Object deleteJobUser(HttpServletRequest request,
                                @RequestParam(required = false, defaultValue = "defaultUser") String username,
                                @RequestParam(required = false, defaultValue = "defaultPass") String password,
                                @RequestParam(required = false, defaultValue = "") String key) {
        String ipAddr = ApiControllerUtil.getIpAddress(request);
        Subject subject = SecurityUtils.getSubject();
        if (subject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request,username, password, subject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, subject, ipAddr, key) ) {
            return deleteJobUser(request);
        }
        return invalidAuthResponse;
    }

    /**
     * Creates a new Api User.
     * @param request Required param(s): name
     * @return GenericResponse indicating success/failure.
     */
    private GenericResponse createApiUser(HttpServletRequest request) {
        GenericResponse response;
        String name = request.getParameter("name");
        String desc = request.getParameter("desc");
        boolean admin = Boolean.parseBoolean(request.getParameter("admin"));

        if (name != null && !name.isEmpty()) {
            ApiUser apiUser = apiUserAuth.addApiUser(name, desc, admin);
            if (apiUser != null) {
                response = new GenericResponse(true, "Added new API User with id " + apiUser.getId());
            }
            else {
                response = new GenericResponse(false, "Failed to add API User. Please ensure name is unique.");
            }
        }
        else {
            response = new GenericResponse(false, "Failed to add API user. A valid name is required!");
        }
        return response;
    }

    /**
     * Deletes an Api User.
     * @param request Required Param(s): id
     * @return GenericResponse indicating success/failure.
     */
    private GenericResponse deleteApiUser(HttpServletRequest request) {
        GenericResponse response;
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            ApiUser apiUserToRemove = apiUserDao.getApiUserById(id);
            if (apiUserToRemove != null) {
                apiUserDao.removeApiUser(apiUserToRemove);
                response = new GenericResponse(true, "Deleted Api User: " + apiUserToRemove.getName());
            }
            else {
                response = new GenericResponse(false, "Could not dekete Api User because it does not exist.");
            }
        }
        catch (NumberFormatException ex) {
            response = new GenericResponse(false, "An Api User Id is required!");
        }
        return response;
    }

    /**
     * Creates a new Job User
     * @param request Required Params: email, password, firstname, lastname
     * @return GenericResponse indicating success/failure.
     */
    private GenericResponse createJobUser(HttpServletRequest request) {
        GenericResponse response;
        String email = request.getParameter("email");
        String firstName = request.getParameter("firstname");
        String lastName = request.getParameter("lastname");
        String password = request.getParameter("password");
        boolean isAdmin = Boolean.parseBoolean(request.getParameter("admin"));

        if (email != null && !email.isEmpty() && password != null && !password.isEmpty()) {
            JobUser jobUser = jobUserAuth.addActiveJobUser(email, password, firstName, lastName, isAdmin);
            if (jobUser != null) {
                response = new GenericResponse(true, "Job User added with id: " + jobUser.getId());
            }
            else {
                response = new GenericResponse(false, "Failed to add Job User. Please ensure email is unique!");
            }
        }
        else {
            response = new GenericResponse(false, "Email and password cannot be empty!");
        }
        return response;
    }

    /**
     * Deletes a Job User with the given id.
     * @param request Required Params: id
     * @return GenericResponse indicating success/failure.
     */
    private GenericResponse deleteJobUser(HttpServletRequest request) {
        int id;
        try {
            id = Integer.parseInt(request.getParameter("id"));
        } catch (NumberFormatException ex) {
            return new GenericResponse(false, "Invalid id supplied");
        }
        JobUser jobUserToDelete = sqlJobUserDao.getJobUserById(id);
        if (jobUserToDelete == null) {
            return new GenericResponse(false, "Job User with id " + id + " does not exist!");
        }
        if (sqlJobUserDao.removeJobUser(jobUserToDelete) == 1) {
            return new GenericResponse(true, "Deleted Job User");
        }
        return new GenericResponse(false, "Failed to delete Job User");
    }
}
