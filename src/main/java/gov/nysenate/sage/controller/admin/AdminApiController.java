package gov.nysenate.sage.controller.admin;

import gov.nysenate.sage.client.response.base.GenericResponse;
import gov.nysenate.sage.client.view.job.JobProcessStatusView;
import gov.nysenate.sage.dao.model.api.SqlApiUserDao;
import gov.nysenate.sage.dao.model.job.SqlJobProcessDao;
import gov.nysenate.sage.dao.model.job.SqlJobUserDao;
import gov.nysenate.sage.dao.stats.api.SqlApiUsageStatsDao;
import gov.nysenate.sage.dao.stats.api.SqlApiUserStatsDao;
import gov.nysenate.sage.dao.stats.deployment.SqlDeploymentStatsDao;
import gov.nysenate.sage.dao.stats.exception.SqlExceptionInfoDao;
import gov.nysenate.sage.dao.stats.geocode.SqlGeocodeStatsDao;
import gov.nysenate.sage.model.api.ApiUser;
import gov.nysenate.sage.model.job.JobProcessStatus;
import gov.nysenate.sage.model.job.JobUser;
import gov.nysenate.sage.model.stats.*;
import gov.nysenate.sage.util.auth.AdminUserAuth;
import gov.nysenate.sage.util.auth.ApiUserAuth;
import gov.nysenate.sage.util.auth.JobUserAuth;
import gov.nysenate.sage.util.controller.ApiControllerUtil;
import gov.nysenate.sage.util.controller.ConstantUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.subject.WebSubject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static gov.nysenate.sage.util.controller.ApiControllerUtil.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = ConstantUtil.ADMIN_REST_PATH + "/api", produces = APPLICATION_JSON_VALUE)
public class AdminApiController
{
    private Logger logger = LoggerFactory.getLogger(AdminApiController.class);

    @Autowired
    @Qualifier("securityManager")
    protected DefaultWebSecurityManager securityManager;

    private SqlApiUserStatsDao sqlApiUserStatsDao;
    private SqlApiUsageStatsDao sqlApiUsageStatsDao;
    private SqlDeploymentStatsDao sqlDeploymentStatsDao;
    private SqlExceptionInfoDao sqlExceptionInfoDao;
    private SqlApiUserDao sqlApiUserDao;
    private SqlJobUserDao sqlJobUserDao;
    private SqlGeocodeStatsDao sqlGeocodeStatsDao;
    private SqlJobProcessDao sqlJobProcessDao;
    private ApiUserAuth apiUserAuth;
    private JobUserAuth jobUserAuth;
    private AdminUserAuth adminUserAuth;

    @Autowired
    public AdminApiController(SqlApiUserStatsDao sqlApiUserStatsDao,
                              SqlApiUsageStatsDao sqlApiUsageStatsDao, SqlDeploymentStatsDao sqlDeploymentStatsDao,
                              SqlExceptionInfoDao sqlExceptionInfoDao, SqlApiUserDao sqlApiUserDao,
                              SqlJobUserDao sqlJobUserDao, SqlGeocodeStatsDao sqlGeocodeStatsDao,
                              SqlJobProcessDao sqlJobProcessDao, ApiUserAuth apiUserAuth, JobUserAuth jobUserAuth,
                              AdminUserAuth adminUserAuth) {
        this.sqlApiUserStatsDao = sqlApiUserStatsDao;
        this.sqlApiUsageStatsDao = sqlApiUsageStatsDao;
        this.sqlDeploymentStatsDao = sqlDeploymentStatsDao;
        this.sqlExceptionInfoDao = sqlExceptionInfoDao;
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
     *
     * Returns the current api users
     *
     * Usage:
     * (GET)    /admin/api/currentApiUsers
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     *
     */
    @RequestMapping(value = "/currentApiUsers", method = RequestMethod.GET)
    public Object currentApiUsers(HttpServletRequest request, HttpServletResponse response,
                                @RequestParam(required = false, defaultValue = "defaultUser") String username,
                                @RequestParam(required = false, defaultValue = "defaultPass") String password,
                                @RequestParam(required = false, defaultValue = "") String key) {
        Object adminResponse;
        String ipAddr= ApiControllerUtil.getIpAddress(request);
        WebSubject webSubject = createSubject(request, response);
        if (webSubject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request,username, password, webSubject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, webSubject, ipAddr, key)) {
            adminResponse = getCurrentApiUsers(request);
        }
        else {
            adminResponse = invalidAuthResponse();
        }
        return adminResponse;
    }

    /**
     * Current Job Users Api
     * ---------------------
     *
     * Returns the current job users
     *
     * Usage:
     * (GET)    /admin/api/currentJobUsers
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     *
     */
    @RequestMapping(value = "/currentJobUsers", method = RequestMethod.GET)
    public Object currentJobUsers(HttpServletRequest request, HttpServletResponse response,
                                @RequestParam(required = false, defaultValue = "defaultUser") String username,
                                @RequestParam(required = false, defaultValue = "defaultPass") String password,
                                @RequestParam(required = false, defaultValue = "") String key) {
        Object adminResponse;
        String ipAddr= ApiControllerUtil.getIpAddress(request);
        WebSubject webSubject = createSubject(request, response);
        if (webSubject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request,username, password, webSubject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, webSubject, ipAddr, key)) {
            adminResponse =  getCurrentJobUsers(request);
        }
        else {
            adminResponse = invalidAuthResponse();
        }
        return adminResponse;

    }

    /**
     * Api User Usage Api
     * ---------------------
     *
     * Returns api user request stats
     *
     * Usage:
     * (GET)    /admin/api/apiUserUsage
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     *
     */
    @RequestMapping(value = "/apiUserUsage", method = RequestMethod.GET)
    public Object apiUserUsage(HttpServletRequest request, HttpServletResponse response,
                             @RequestParam(required = false, defaultValue = "defaultUser") String username,
                             @RequestParam(required = false, defaultValue = "defaultPass") String password,
                             @RequestParam(required = false, defaultValue = "") String key) {
        Object adminResponse;
        String ipAddr= ApiControllerUtil.getIpAddress(request);
        WebSubject webSubject = createSubject(request, response);
        if (webSubject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request,username, password, webSubject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, webSubject, ipAddr, key)) {
            adminResponse = getApiUserStats(request);
        }
        else {
            adminResponse = invalidAuthResponse();
        }
        return adminResponse;
    }

    /**
     * Api Usage Api
     * ---------------------
     *
     * Returns api user usage request stats
     *
     * Usage:
     * (GET)    /admin/api/usage
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     *
     */
    @RequestMapping(value = "/usage", method = RequestMethod.GET)
    public Object usage(HttpServletRequest request, HttpServletResponse response,
                      @RequestParam(required = false, defaultValue = "defaultUser") String username,
                      @RequestParam(required = false, defaultValue = "defaultPass") String password,
                      @RequestParam(required = false, defaultValue = "") String key) {
        Object adminResponse;
        String ipAddr= ApiControllerUtil.getIpAddress(request);
        WebSubject webSubject = createSubject(request, response);
        if (webSubject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request,username, password, webSubject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, webSubject, ipAddr, key)) {
            adminResponse = getApiUsageStats(request);
        }
        else {
            adminResponse = invalidAuthResponse();
        }
        return adminResponse;
    }

    /**
     * Geocode Usage Api
     * ---------------------
     *
     * Returns geocode usage stats
     *
     * Usage:
     * (GET)    /admin/api/geocodeUsage
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     *
     */
    @RequestMapping(value = "/geocodeUsage", method = RequestMethod.GET)
    public Object geocodeUsage(HttpServletRequest request, HttpServletResponse response,
                             @RequestParam(required = false, defaultValue = "defaultUser") String username,
                             @RequestParam(required = false, defaultValue = "defaultPass") String password,
                             @RequestParam(required = false, defaultValue = "") String key) {
        Object adminResponse;
        String ipAddr= ApiControllerUtil.getIpAddress(request);
        WebSubject webSubject = createSubject(request, response);
        if (webSubject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request,username, password, webSubject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, webSubject, ipAddr, key)) {
            adminResponse = getGeocodeUsageStats(request);
        }
        else {
            adminResponse = invalidAuthResponse();
        }
        return adminResponse;
    }

    /**
     * Job Statuses Api
     * ---------------------
     *
     * Returns current job statuses
     *
     * Usage:
     * (GET)    /admin/api/jobStatuses
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     *
     */
    @RequestMapping(value = "/jobStatuses", method = RequestMethod.GET)
    public Object jobStatuses(HttpServletRequest request, HttpServletResponse response,
                            @RequestParam(required = false, defaultValue = "defaultUser") String username,
                            @RequestParam(required = false, defaultValue = "defaultPass") String password,
                            @RequestParam(required = false, defaultValue = "") String key) {
        Object adminResponse;
        String ipAddr= ApiControllerUtil.getIpAddress(request);
        WebSubject webSubject = createSubject(request, response);
        if (webSubject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request,username, password, webSubject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, webSubject, ipAddr, key)) {
            adminResponse = getJobProcessStatusList(request);
        }
        else {
            adminResponse = invalidAuthResponse();
        }
        return adminResponse;
    }

    /**
     * Deployment Stats Api
     * ---------------------
     *
     * Returns deployment stats
     *
     * Usage:
     * (GET)    /admin/api/deployment
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     *
     */
    @RequestMapping(value = "/deployment", method = RequestMethod.GET)
    public Object deployment(HttpServletRequest request, HttpServletResponse response,
                           @RequestParam(required = false, defaultValue = "defaultUser") String username,
                           @RequestParam(required = false, defaultValue = "defaultPass") String password,
                           @RequestParam(required = false, defaultValue = "") String key) {
        Object adminResponse;
        String ipAddr= ApiControllerUtil.getIpAddress(request);
        WebSubject webSubject = createSubject(request, response);
        if (webSubject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request,username, password, webSubject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, webSubject, ipAddr, key)) {
            adminResponse = getDeploymentStats(request);
        }
        else {
            adminResponse = invalidAuthResponse();
        }
        return adminResponse;
    }

    /**
     * Exception Stats Api
     * ---------------------
     *
     * Returns exception stats
     *
     * Usage:
     * (GET)    /admin/api/exception
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     *
     */
    @RequestMapping(value = "/exception", method = RequestMethod.GET)
    public Object exception(HttpServletRequest request, HttpServletResponse response,
                          @RequestParam(required = false, defaultValue = "defaultUser") String username,
                          @RequestParam(required = false, defaultValue = "defaultPass") String password,
                          @RequestParam(required = false, defaultValue = "") String key) {
        Object adminResponse;
        String ipAddr= ApiControllerUtil.getIpAddress(request);
        WebSubject webSubject = createSubject(request, response);
        if (webSubject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request,username, password, webSubject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, webSubject, ipAddr, key)) {
            adminResponse = getExceptionStats(request);
        }
        else {
            adminResponse = invalidAuthResponse();
        }
        return adminResponse;
    }


    /**
     * Create Api Users Api
     * ---------------------
     *
     * Creates an api user
     *
     * Usage:
     * (POST)    /admin/api/createApiUser
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     *
     */
    @RequestMapping(value = "/createApiUser", method = RequestMethod.POST)
    public Object createApiUser(HttpServletRequest request, HttpServletResponse response,
                              @RequestParam(required = false, defaultValue = "defaultUser") String username,
                              @RequestParam(required = false, defaultValue = "defaultPass") String password,
                              @RequestParam(required = false, defaultValue = "") String key) {
        Object adminResponse;
        String ipAddr= ApiControllerUtil.getIpAddress(request);
        WebSubject webSubject = createSubject(request, response);
        if (webSubject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request,username, password, webSubject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, webSubject, ipAddr, key)) {
            adminResponse = createApiUser(request);
        }
        else {
            adminResponse = invalidAuthResponse();
        }
        return adminResponse;

    }

    /**
     * Delete Api Users Api
     * ---------------------
     *
     * Deletes an api user
     *
     * Usage:
     * (POST)    /admin/api/deleteApiUser
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     *
     */
    @RequestMapping(value = "/deleteApiUser", method = RequestMethod.POST)
    public Object deleteApiUser(HttpServletRequest request, HttpServletResponse response,
                              @RequestParam(required = false, defaultValue = "defaultUser") String username,
                              @RequestParam(required = false, defaultValue = "defaultPass") String password,
                              @RequestParam(required = false, defaultValue = "") String key) {
        Object adminResponse;
        String ipAddr= ApiControllerUtil.getIpAddress(request);
        WebSubject webSubject = createSubject(request, response);
        if (webSubject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request,username, password, webSubject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, webSubject, ipAddr, key)) {
            adminResponse = deleteApiUser(request);
        }
        else {
            adminResponse = invalidAuthResponse();
        }
        return adminResponse;
    }

    /**
     * Create Job Users Api
     * ---------------------
     *
     * Creates a job user
     *
     * Usage:
     * (POST)    /admin/api/createJobUser
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     *
     */
    @RequestMapping(value = "/createJobUser", method = RequestMethod.POST)
    public Object createJobUser(HttpServletRequest request, HttpServletResponse response,
                              @RequestParam(required = false, defaultValue = "defaultUser") String username,
                              @RequestParam(required = false, defaultValue = "defaultPass") String password,
                              @RequestParam(required = false, defaultValue = "") String key) {
        Object adminResponse;
        String ipAddr= ApiControllerUtil.getIpAddress(request);
        WebSubject webSubject = createSubject(request, response);
        if (webSubject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request,username, password, webSubject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, webSubject, ipAddr, key)){
            adminResponse = createJobUser(request);
        }
        else {
            adminResponse = invalidAuthResponse();
        }
        return adminResponse;
    }

    /**
     * Delete Job Users Api
     * ---------------------
     *
     * Deletes a job user
     *
     * Usage:
     * (POST)    /admin/api/deleteJobUser
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     *
     */
    @RequestMapping(value = "/deleteJobUser", method = RequestMethod.POST)
    public Object deleteJobUser(HttpServletRequest request, HttpServletResponse response,
                              @RequestParam(required = false, defaultValue = "defaultUser") String username,
                              @RequestParam(required = false, defaultValue = "defaultPass") String password,
                              @RequestParam(required = false, defaultValue = "") String key) {
        Object adminResponse;
        String ipAddr= ApiControllerUtil.getIpAddress(request);
        WebSubject webSubject = createSubject(request, response);
        if (webSubject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request,username, password, webSubject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, webSubject, ipAddr, key)) {
            adminResponse = deleteJobUser(request);
        }
        else {
            adminResponse = invalidAuthResponse();
        }
        return adminResponse;
    }

    /**
     * Hide exception Api
     * ---------------------
     *
     * Hides an exception from the admin user interface
     *
     * Usage:
     * (POST)    /admin/api/hideException
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     *
     */
    @RequestMapping(value = "/hideException", method = RequestMethod.POST)
    public Object hideException(HttpServletRequest request, HttpServletResponse response,
                              @RequestParam(required = false, defaultValue = "defaultUser") String username,
                              @RequestParam(required = false, defaultValue = "defaultPass") String password,
                              @RequestParam(required = false, defaultValue = "") String key) {
        Object adminResponse;
        String ipAddr= ApiControllerUtil.getIpAddress(request);
        WebSubject webSubject = createSubject(request, response);
        if (webSubject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request,username, password, webSubject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, webSubject, ipAddr, key)) {
            adminResponse = hideException(request);
        }
        else {
            adminResponse = invalidAuthResponse();
        }
        return adminResponse;
    }

    /**
     * Retrieves all registered Api Users. This method should not be exposed through a non-admin API as it contains
     * the hashed api user passwords.
     * @param request HttpServletRequest
     * @return List<ApiUser>
     */
    private List getCurrentApiUsers(HttpServletRequest request)
    {
        return sqlApiUserDao.getApiUsers();
    }

    /**
     * Retrieves all registered Job Users.
     * @param request
     * @return
     */
    private List getCurrentJobUsers(HttpServletRequest request)
    {
        return sqlJobUserDao.getJobUsers();
    }

    /**
     * Creates a new Api User.
     * @param request Required param(s): name
     * @return GenericResponse indicating success/failure.
     */
    private GenericResponse createApiUser(HttpServletRequest request)
    {
        GenericResponse response;
        String name = request.getParameter("name");
        String desc = request.getParameter("desc");
        boolean admin = Boolean.parseBoolean( request.getParameter("admin") );

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
    private GenericResponse deleteApiUser(HttpServletRequest request)
    {
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
    private GenericResponse createJobUser(HttpServletRequest request)
    {
        GenericResponse response;
        String email = request.getParameter("email");
        String firstName = request.getParameter("firstname");
        String lastName = request.getParameter("lastname");
        String password = request.getParameter("password");
        Boolean isAdmin = Boolean.parseBoolean(request.getParameter("admin"));

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
    private GenericResponse deleteJobUser(HttpServletRequest request)
    {
        GenericResponse response;
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            JobUser jobUserToDelete = sqlJobUserDao.getJobUserById(id);
            if (jobUserToDelete != null) {
                int status = sqlJobUserDao.removeJobUser(jobUserToDelete);
                if (status == 1) {
                    response = new GenericResponse(true, "Deleted Job User");
                }
                else {
                    response = new GenericResponse(false, "Failed to delete Job User");
                }
            }
            else {
                response = new GenericResponse(false, "Job User with id " + id + " does not exist!");
            }
        }
        catch (NumberFormatException ex) {
            response = new GenericResponse(false, "Invalid id supplied");
        }
        return response;
    }

    /**
     * Returns an object that contains a list of the times of all application deployments and shutdowns.
     * @see DeploymentStats
     * @param request HttpServletRequest
     * @return DeploymentStats
     */
    private DeploymentStats getDeploymentStats(HttpServletRequest request)
    {
        DeploymentStats deploymentStats = sqlDeploymentStatsDao.getDeploymentStats();
        return deploymentStats;
    }

    /**
     * Retrieves interval-based api usage stats within a specified time frame or per hour by default.
     * @see ApiUsageStats
     * @see SqlApiUsageStatsDao.RequestInterval
     * @param request HttpServletRequest with optional query parameter 'interval' which should be a
     *                string representation of a RequestInterval value (e.g 'HOUR').
     * @return ApiUsageStats
     */
    private ApiUsageStats getApiUsageStats(HttpServletRequest request)
    {
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
     * Returns GeocodeUsageStats from `sinceDays` ago to now.
     * @param request: HttpServletRequest, Optional query parameter: sinceDays (int)
     * @return GeocodeStats
     */
    private GeocodeStats getGeocodeUsageStats(HttpServletRequest request)
    {
        Integer sinceDays = null;
        try {
            sinceDays = Integer.parseInt(request.getParameter("sinceDays"));
        }
        catch (NumberFormatException ex) {}
        return sqlGeocodeStatsDao.getGeocodeStats(getBeginTimestamp(request), getEndTimestamp(request));
    }

    /**
     * Returns a List of JobProcessStatus objects within the given 'from' and 'to' request time range.
     * @param request HttpServletRequest, Optional Params: 'from' (Start timestamp value for job requestTime)
     *                                                     'to' (End timestamp value for job requestTime)
     * @return List<JobProcessStatus>
     */
    private List<JobProcessStatusView> getJobProcessStatusList(HttpServletRequest request)
    {
        List<JobProcessStatus> statuses = new ArrayList<>();
        List<JobProcessStatusView> statusViews = new ArrayList<>();
        Timestamp from = getBeginTimestamp(request);
        Timestamp to = getEndTimestamp(request);
        statuses = sqlJobProcessDao.getJobStatusesByConditions(Arrays.asList(JobProcessStatus.Condition.values()), null, from, to);
        for (JobProcessStatus jobProcessStatus : statuses) {
            statusViews.add(new JobProcessStatusView(jobProcessStatus));
        }
        return statusViews;
    }

    private Map<Integer, ApiUserStats> getApiUserStats(HttpServletRequest request)
    {
        return sqlApiUserStatsDao.getRequestCounts(getBeginTimestamp(request), getEndTimestamp(request));
    }

    private List<ExceptionInfo> getExceptionStats(HttpServletRequest request)
    {
        return sqlExceptionInfoDao.getExceptionInfoList(true);
    }

    /**
     * Marks an exception as hidden so that it can be filtered out in the interface.
     * @param request Required Params: id (of the exceptionInfo).
     * @return GenericResponse indicating success/failure.
     */
    private GenericResponse hideException(HttpServletRequest request)
    {
        int id;
        try {
            id = Integer.parseInt(request.getParameter("id"));
        }
        catch (NumberFormatException ex) {
            return new GenericResponse(false, "Must supply a valid exception id to hide!");
        }
        int update = sqlExceptionInfoDao.hideExceptionInfo(id);
        return (update > 0) ? new GenericResponse(true, "Exception hidden")
                            : new GenericResponse(false, "Failed to hide exception!");
    }

    protected WebSubject createSubject(ServletRequest request, ServletResponse response) {
        return new WebSubject.Builder(securityManager, request, response).buildWebSubject();
    }
}
