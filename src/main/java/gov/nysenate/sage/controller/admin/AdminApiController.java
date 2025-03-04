package gov.nysenate.sage.controller.admin;

import gov.nysenate.sage.client.response.base.GenericResponse;
import gov.nysenate.sage.client.view.job.JobProcessStatusView;
import gov.nysenate.sage.dao.model.api.SqlApiUserDao;
import gov.nysenate.sage.dao.model.job.SqlJobProcessDao;
import gov.nysenate.sage.dao.model.job.SqlJobUserDao;
import gov.nysenate.sage.dao.stats.api.SqlApiUsageStatsDao;
import gov.nysenate.sage.dao.stats.api.SqlApiUserStatsDao;
import gov.nysenate.sage.dao.stats.deployment.SqlDeploymentStatsDao;
import gov.nysenate.sage.dao.stats.geocode.SqlGeocodeStatsDao;
import gov.nysenate.sage.model.api.ApiUser;
import gov.nysenate.sage.model.job.JobProcessStatus;
import gov.nysenate.sage.model.job.JobUser;
import gov.nysenate.sage.model.stats.ApiUsageStats;
import gov.nysenate.sage.model.stats.DeploymentStats;
import gov.nysenate.sage.util.auth.AdminUserAuth;
import gov.nysenate.sage.util.auth.ApiUserAuth;
import gov.nysenate.sage.util.auth.JobUserAuth;
import gov.nysenate.sage.util.controller.ApiControllerUtil;
import gov.nysenate.sage.util.controller.ConstantUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static gov.nysenate.sage.util.controller.ApiControllerUtil.*;

@RestController
@RequestMapping(value = ConstantUtil.ADMIN_REST_PATH + "/api")
public class AdminApiController {
    private static final Logger logger = LoggerFactory.getLogger(AdminApiController.class);

    private final SqlApiUserStatsDao sqlApiUserStatsDao;
    private final SqlApiUsageStatsDao sqlApiUsageStatsDao;
    private final SqlDeploymentStatsDao sqlDeploymentStatsDao;
    private final SqlApiUserDao sqlApiUserDao;
    private final SqlJobUserDao sqlJobUserDao;
    private final SqlGeocodeStatsDao sqlGeocodeStatsDao;
    private final SqlJobProcessDao sqlJobProcessDao;
    private final ApiUserAuth apiUserAuth;
    private final JobUserAuth jobUserAuth;
    private final AdminUserAuth adminUserAuth;

    @Autowired
    public AdminApiController(SqlApiUserStatsDao sqlApiUserStatsDao,
                              SqlApiUsageStatsDao sqlApiUsageStatsDao, SqlDeploymentStatsDao sqlDeploymentStatsDao,
                              SqlApiUserDao sqlApiUserDao, SqlJobUserDao sqlJobUserDao,
                              SqlGeocodeStatsDao sqlGeocodeStatsDao, SqlJobProcessDao sqlJobProcessDao,
                              ApiUserAuth apiUserAuth, JobUserAuth jobUserAuth, AdminUserAuth adminUserAuth) {
        this.sqlApiUserStatsDao = sqlApiUserStatsDao;
        this.sqlApiUsageStatsDao = sqlApiUsageStatsDao;
        this.sqlDeploymentStatsDao = sqlDeploymentStatsDao;
        this.sqlApiUserDao = sqlApiUserDao;
        this.sqlJobUserDao = sqlJobUserDao;
        this.sqlGeocodeStatsDao = sqlGeocodeStatsDao;
        this.sqlJobProcessDao = sqlJobProcessDao;
        this.apiUserAuth = apiUserAuth;
        this.jobUserAuth = jobUserAuth;
        this.adminUserAuth = adminUserAuth;
    }

    /**
     * Current Api Users Api
     * ---------------------
     * Returns the current api users
     * Usage:
     * (GET)    /admin/api/currentApiUsers
     *
     */
    @GetMapping(value = "/currentApiUsers")
    public void currentApiUsers(HttpServletRequest request, HttpServletResponse response,
                                @RequestParam(required = false, defaultValue = "defaultUser") String username,
                                @RequestParam(required = false, defaultValue = "defaultPass") String password,
                                @RequestParam(required = false, defaultValue = "") String key) {
        Object adminResponse;
        String ipAddr = ApiControllerUtil.getIpAddress(request);
        Subject subject = SecurityUtils.getSubject();
        if (subject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request,username, password, subject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, subject, ipAddr, key)) {
            adminResponse = sqlApiUserDao.getApiUsers();
        }
        else {
            adminResponse = invalidAuthResponse();
        }
        setAdminResponse(adminResponse, response);

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
    public void currentJobUsers(HttpServletRequest request, HttpServletResponse response,
                                @RequestParam(required = false, defaultValue = "defaultUser") String username,
                                @RequestParam(required = false, defaultValue = "defaultPass") String password,
                                @RequestParam(required = false, defaultValue = "") String key) {
        Object adminResponse;
        String ipAddr= ApiControllerUtil.getIpAddress(request);
        Subject subject = SecurityUtils.getSubject();
        if (subject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request,username, password, subject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, subject, ipAddr, key) ) {
            adminResponse = sqlJobUserDao.getJobUsers();
        }
        else {
            adminResponse = invalidAuthResponse();
        }
        setAdminResponse(adminResponse, response);

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
    public void apiUserUsage(HttpServletRequest request, HttpServletResponse response,
                             @RequestParam(required = false, defaultValue = "defaultUser") String username,
                             @RequestParam(required = false, defaultValue = "defaultPass") String password,
                             @RequestParam(required = false, defaultValue = "") String key) {
        Object adminResponse;
        String ipAddr= ApiControllerUtil.getIpAddress(request);
        Subject subject = SecurityUtils.getSubject();
        if (subject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request, username, password, subject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, subject, ipAddr, key)) {
            adminResponse = sqlApiUserStatsDao.getRequestCounts(getBeginTimestamp(request), getEndTimestamp(request));
        }
        else {
            adminResponse = invalidAuthResponse();
        }
        setAdminResponse(adminResponse, response);

    }

    /**
     * Api Usage Api
     * ---------------------
     * Returns api user usage request stats
     * Usage:
     * (GET)    /admin/api/usage
     *
     */
    @GetMapping(value = "/usage")
    public void usage(HttpServletRequest request, HttpServletResponse response,
                      @RequestParam(required = false, defaultValue = "defaultUser") String username,
                      @RequestParam(required = false, defaultValue = "defaultPass") String password,
                      @RequestParam(required = false, defaultValue = "") String key) {
        Object adminResponse;
        String ipAddr = ApiControllerUtil.getIpAddress(request);
        Subject subject = SecurityUtils.getSubject();
        if (subject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request, username, password, subject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, subject, ipAddr, key)) {
            adminResponse = getApiUsageStats(request);
        }
        else {
            adminResponse = invalidAuthResponse();
        }
        setAdminResponse(adminResponse, response);

    }

    /**
     * Geocode Usage Api
     * ---------------------
     * Returns geocode usage stats
     * Usage:
     * (GET)    /admin/api/geocodeUsage
     *
     */
    @GetMapping(value = "/geocodeUsage")
    public void geocodeUsage(HttpServletRequest request, HttpServletResponse response,
                             @RequestParam(required = false, defaultValue = "defaultUser") String username,
                             @RequestParam(required = false, defaultValue = "defaultPass") String password,
                             @RequestParam(required = false, defaultValue = "") String key) {
        Object adminResponse;
        String ipAddr= ApiControllerUtil.getIpAddress(request);
        Subject subject = SecurityUtils.getSubject();
        if (subject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request,username, password, subject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, subject, ipAddr, key) ) {
            adminResponse = sqlGeocodeStatsDao.getGeocodeStats(getBeginTimestamp(request), getEndTimestamp(request));
        }
        else {
            adminResponse = invalidAuthResponse();
        }
        setAdminResponse(adminResponse, response);

    }

    /**
     * Job Statuses Api
     * ---------------------
     * Returns current job statuses
     * Usage:
     * (GET)    /admin/api/jobStatuses
     *
     */
    @GetMapping(value = "/jobStatuses")
    public void jobStatuses(HttpServletRequest request, HttpServletResponse response,
                            @RequestParam(required = false, defaultValue = "defaultUser") String username,
                            @RequestParam(required = false, defaultValue = "defaultPass") String password,
                            @RequestParam(required = false, defaultValue = "") String key) {
        Object adminResponse;
        String ipAddr= ApiControllerUtil.getIpAddress(request);
        Subject subject = SecurityUtils.getSubject();
        if (subject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request,username, password, subject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, subject, ipAddr, key) ) {
            adminResponse = getJobProcessStatusList(request);
        }
        else {
            adminResponse = invalidAuthResponse();
        }
        setAdminResponse(adminResponse, response);

    }

    /**
     * Deployment Stats Api
     * ---------------------
     * Returns deployment stats
     * Usage:
     * (GET)    /admin/api/deployment
     *
     */
    @GetMapping(value = "/deployment")
    public void deployment(HttpServletRequest request, HttpServletResponse response,
                           @RequestParam(required = false, defaultValue = "defaultUser") String username,
                           @RequestParam(required = false, defaultValue = "defaultPass") String password,
                           @RequestParam(required = false, defaultValue = "") String key) {
        Object adminResponse;
        String ipAddr= ApiControllerUtil.getIpAddress(request);
        Subject subject = SecurityUtils.getSubject();
        if (subject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request,username, password, subject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, subject, ipAddr, key)) {
            adminResponse = new DeploymentStats(sqlDeploymentStatsDao.getDeploymentStats());
        }
        else {
            adminResponse = invalidAuthResponse();
        }
        setAdminResponse(adminResponse, response);

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
    public void createApiUser(HttpServletRequest request, HttpServletResponse response,
                              @RequestParam(required = false, defaultValue = "defaultUser") String username,
                              @RequestParam(required = false, defaultValue = "defaultPass") String password,
                              @RequestParam(required = false, defaultValue = "") String key) {
        Object adminResponse;
        String ipAddr= ApiControllerUtil.getIpAddress(request);
        Subject subject = SecurityUtils.getSubject();
        if (subject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request,username, password, subject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, subject, ipAddr, key) ) {
            adminResponse = createApiUser(request);
        }
        else {
            adminResponse = invalidAuthResponse();
        }
        setAdminResponse(adminResponse, response);


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
    public void deleteApiUser(HttpServletRequest request, HttpServletResponse response,
                              @RequestParam(required = false, defaultValue = "defaultUser") String username,
                              @RequestParam(required = false, defaultValue = "defaultPass") String password,
                              @RequestParam(required = false, defaultValue = "") String key) {
        Object adminResponse;
        String ipAddr= ApiControllerUtil.getIpAddress(request);
        Subject subject = SecurityUtils.getSubject();
        if (subject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request,username, password, subject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, subject, ipAddr, key) ) {
            adminResponse = deleteApiUser(request);
        }
        else {
            adminResponse = invalidAuthResponse();
        }
        setAdminResponse(adminResponse, response);

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
    public void createJobUser(HttpServletRequest request, HttpServletResponse response,
                              @RequestParam(required = false, defaultValue = "defaultUser") String username,
                              @RequestParam(required = false, defaultValue = "defaultPass") String password,
                              @RequestParam(required = false, defaultValue = "") String key) {
        Object adminResponse;
        String ipAddr= ApiControllerUtil.getIpAddress(request);
        Subject subject = SecurityUtils.getSubject();
        if (subject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request,username, password, subject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, subject, ipAddr, key)) {
            adminResponse = createJobUser(request);
        }
        else {
            adminResponse = invalidAuthResponse();
        }
        setAdminResponse(adminResponse, response);

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
    public void deleteJobUser(HttpServletRequest request, HttpServletResponse response,
                              @RequestParam(required = false, defaultValue = "defaultUser") String username,
                              @RequestParam(required = false, defaultValue = "defaultPass") String password,
                              @RequestParam(required = false, defaultValue = "") String key) {
        Object adminResponse;
        String ipAddr = ApiControllerUtil.getIpAddress(request);
        Subject subject = SecurityUtils.getSubject();
        if (subject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request,username, password, subject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, subject, ipAddr, key) ) {
            adminResponse = deleteJobUser(request);
        }
        else {
            adminResponse = invalidAuthResponse();
        }
        setAdminResponse(adminResponse, response);

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
            ApiUser apiUserToRemove = sqlApiUserDao.getApiUserById(id);
            if (apiUserToRemove != null) {
                sqlApiUserDao.removeApiUser(apiUserToRemove);
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

    /**
     * Retrieves interval-based api usage stats within a specified time frame or per hour by default.
     * @see ApiUsageStats
     * @see SqlApiUsageStatsDao.RequestInterval
     * @param request HttpServletRequest with optional query parameter 'interval' which should be a
     *                string representation of a RequestInterval value (e.g 'HOUR').
     * @return ApiUsageStats
     */
    private ApiUsageStats getApiUsageStats(HttpServletRequest request) {
        SqlApiUsageStatsDao.RequestInterval requestInterval;
        try {
            requestInterval = SqlApiUsageStatsDao.RequestInterval.valueOf(request.getParameter("interval"));
        }
        catch (Exception ex) {
            logger.warn("Invalid interval parameter; defaulting to HOUR.");
            requestInterval = SqlApiUsageStatsDao.RequestInterval.HOUR;
        }

        return sqlApiUsageStatsDao.getApiUsageStats(getBeginTimestamp(request), getEndTimestamp(request), requestInterval);
    }

    /**
     * Returns a List of JobProcessStatus objects within the given 'from' and 'to' request time range.
     * @param request HttpServletRequest, Optional Params: 'from' (Start timestamp value for job requestTime)
     *                                                     'to' (End timestamp value for job requestTime)
     * @return List<JobProcessStatus>
     */
    private List<JobProcessStatusView> getJobProcessStatusList(HttpServletRequest request) {
        List<JobProcessStatusView> statusViews = new ArrayList<>();
        Timestamp from = getBeginTimestamp(request);
        Timestamp to = getEndTimestamp(request);
        List<JobProcessStatus> statuses = sqlJobProcessDao.getJobStatusesByConditions(
                List.of(JobProcessStatus.Condition.values()), null, from, to
        );
        for (JobProcessStatus jobProcessStatus : statuses) {
            statusViews.add(new JobProcessStatusView(jobProcessStatus));
        }
        return statusViews;
    }

}
