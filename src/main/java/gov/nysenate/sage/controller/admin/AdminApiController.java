package gov.nysenate.sage.controller.admin;

import gov.nysenate.sage.client.response.base.GenericResponse;
import gov.nysenate.sage.client.view.job.JobProcessStatusView;
import gov.nysenate.sage.dao.logger.ApiRequestLogger;
import gov.nysenate.sage.dao.model.ApiUserDao;
import gov.nysenate.sage.dao.model.JobProcessDao;
import gov.nysenate.sage.dao.model.JobUserDao;
import gov.nysenate.sage.dao.stats.*;
import gov.nysenate.sage.model.api.ApiUser;
import gov.nysenate.sage.model.job.JobProcessStatus;
import gov.nysenate.sage.model.job.JobUser;
import gov.nysenate.sage.model.stats.*;
import gov.nysenate.sage.util.auth.ApiUserAuth;
import gov.nysenate.sage.util.auth.JobUserAuth;
import gov.nysenate.sage.util.controller.ConstantUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static gov.nysenate.sage.util.controller.ApiControllerUtil.*;

@Controller
@RequestMapping(value = ConstantUtil.REST_PATH + "admin")
public class AdminApiController
{
    private Logger logger = LogManager.getLogger(AdminApiController.class);

    private ApiRequestLogger apiRequestLogger;
    private ApiUserStatsDao apiUserStatsDao;
    private ApiUsageStatsDao apiUsageStatsDao;
    private DeploymentStatsDao deploymentStatsDao;

    @Autowired
    public AdminApiController(ApiRequestLogger apiRequestLogger, ApiUserStatsDao apiUserStatsDao,
                              ApiUsageStatsDao apiUsageStatsDao, DeploymentStatsDao deploymentStatsDao) {
        this.apiRequestLogger = apiRequestLogger;
        this.apiUserStatsDao = apiUserStatsDao;
        this.apiUsageStatsDao = apiUsageStatsDao;
        this.deploymentStatsDao = deploymentStatsDao;
    }

    @RequestMapping(value = "/currentApiUsers", method = RequestMethod.GET)
    public void currentApiUsers(HttpServletRequest request, HttpServletResponse response) {
        Object adminResponse;
        if (isAuthenticated(request) ) {
            adminResponse = getCurrentApiUsers(request);
        }
        else {
            adminResponse = invalidAuthResponse();
        }
        setAdminResponse(adminResponse, response);

    }

    @RequestMapping(value = "/currentJobUsers", method = RequestMethod.GET)
    public void currentJobUsers(HttpServletRequest request, HttpServletResponse response) {
        Object adminResponse;
        if (isAuthenticated(request) ) {
            adminResponse =  getCurrentJobUsers(request);
        }
        else {
            adminResponse = invalidAuthResponse();
        }
        setAdminResponse(adminResponse, response);

    }

    @RequestMapping(value = "/apiUserUsage", method = RequestMethod.GET)
    public void apiUserUsage(HttpServletRequest request, HttpServletResponse response) {
        Object adminResponse;
        if (isAuthenticated(request) ) {
            adminResponse = getApiUserStats(request);
        }
        else {
            adminResponse = invalidAuthResponse();
        }
        setAdminResponse(adminResponse, response);

    }

    @RequestMapping(value = "/usage", method = RequestMethod.GET)
    public void usage(HttpServletRequest request, HttpServletResponse response) {
        Object adminResponse;
        if (isAuthenticated(request) ) {
            adminResponse = getApiUsageStats(request);
        }
        else {
            adminResponse = invalidAuthResponse();
        }
        setAdminResponse(adminResponse, response);

    }

    @RequestMapping(value = "/geocodeUsage", method = RequestMethod.GET)
    public void geocodeUsage(HttpServletRequest request, HttpServletResponse response) {
        Object adminResponse;
        if (isAuthenticated(request) ) {
            adminResponse = getGeocodeUsageStats(request);
        }
        else {
            adminResponse = invalidAuthResponse();
        }
        setAdminResponse(adminResponse, response);

    }

    @RequestMapping(value = "/jobStatuses", method = RequestMethod.GET)
    public void jobStatuses(HttpServletRequest request, HttpServletResponse response) {
        Object adminResponse;
        if (isAuthenticated(request) ) {
            adminResponse = getJobProcessStatusList(request);
        }
        else {
            adminResponse = invalidAuthResponse();
        }
        setAdminResponse(adminResponse, response);

    }

    @RequestMapping(value = "/deployment", method = RequestMethod.GET)
    public void deployment(HttpServletRequest request, HttpServletResponse response) {
        Object adminResponse;
        if (isAuthenticated(request) ) {
            adminResponse = getDeploymentStats(request);
        }
        else {
            adminResponse = invalidAuthResponse();
        }
        setAdminResponse(adminResponse, response);

    }

    @RequestMapping(value = "/exception", method = RequestMethod.GET)
    public void exception(HttpServletRequest request, HttpServletResponse response) {
        Object adminResponse;
        if (isAuthenticated(request) ) {
            adminResponse = getExceptionStats(request);
        }
        else {
            adminResponse = invalidAuthResponse();
        }
        setAdminResponse(adminResponse, response);

    }



    @RequestMapping(value = "/createApiUser", method = RequestMethod.POST)
    public void createApiUser(HttpServletRequest request, HttpServletResponse response) {
        Object adminResponse;
        if (isAuthenticated(request) ) {
            adminResponse = createApiUser(request);
        }
        else {
            adminResponse = invalidAuthResponse();
        }
        setAdminResponse(adminResponse, response);


    }
    @RequestMapping(value = "/deleteApiUser", method = RequestMethod.POST)
    public void deleteApiUser(HttpServletRequest request, HttpServletResponse response) {
        Object adminResponse;
        if (isAuthenticated(request) ) {
            adminResponse = deleteApiUser(request);
        }
        else {
            adminResponse = invalidAuthResponse();
        }
        setAdminResponse(adminResponse, response);

    }
    @RequestMapping(value = "/createJobUser", method = RequestMethod.POST)
    public void createJobUser(HttpServletRequest request, HttpServletResponse response) {
        Object adminResponse;
        if (isAuthenticated(request) ) {
            adminResponse = createJobUser(request);
        }
        else {
            adminResponse = invalidAuthResponse();
        }
        setAdminResponse(adminResponse, response);

    }
    @RequestMapping(value = "/deleteJobUser", method = RequestMethod.POST)
    public void deleteJobUser(HttpServletRequest request, HttpServletResponse response) {
        Object adminResponse;
        if (isAuthenticated(request) ) {
            adminResponse = deleteJobUser(request);
        }
        else {
            adminResponse = invalidAuthResponse();
        }
        setAdminResponse(adminResponse, response);

    }
    @RequestMapping(value = "/hideException", method = RequestMethod.POST)
    public void hideException(HttpServletRequest request, HttpServletResponse response) {
        Object adminResponse;
        if (isAuthenticated(request) ) {
            adminResponse = hideException(request);
        }
        else {
            adminResponse = invalidAuthResponse();
        }
        setAdminResponse(adminResponse, response);

    }

    private GenericResponse invalidAuthResponse() {
        return new GenericResponse(false, "You must be logged in as an administrator to access this API.");
    }

    //adminResponse = new GenericResponse(false, "Invalid admin API request.");

    /**
     * Retrieves all registered Api Users. This method should not be exposed through a non-admin API as it contains
     * the hashed api user passwords.
     * @param request HttpServletRequest
     * @return List<ApiUser>
     */
    private List getCurrentApiUsers(HttpServletRequest request)
    {
        ApiUserDao apiUserDao = new ApiUserDao();
        return apiUserDao.getApiUsers();
    }

    /**
     * Retrieves all registered Job Users.
     * @param request
     * @return
     */
    private List getCurrentJobUsers(HttpServletRequest request)
    {
        JobUserDao jobUserDao = new JobUserDao();
        return jobUserDao.getJobUsers();
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

        if (name != null && !name.isEmpty()) {
            ApiUserAuth apiUserAuth = new ApiUserAuth();
            ApiUser apiUser = apiUserAuth.addApiUser(name, desc);
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
            ApiUserDao apiUserDao = new ApiUserDao();
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
    private GenericResponse createJobUser(HttpServletRequest request)
    {
        GenericResponse response;
        String email = request.getParameter("email");
        String firstName = request.getParameter("firstname");
        String lastName = request.getParameter("lastname");
        String password = request.getParameter("password");
        Boolean isAdmin = Boolean.parseBoolean(request.getParameter("admin"));

        if (email != null && !email.isEmpty() && password != null && !password.isEmpty()) {
            JobUserAuth jobUserAuth = new JobUserAuth();
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
            JobUserDao jobUserDao = new JobUserDao();
            JobUser jobUserToDelete = jobUserDao.getJobUserById(id);
            if (jobUserToDelete != null) {
                int status = jobUserDao.removeJobUser(jobUserToDelete);
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
        DeploymentStats deploymentStats = deploymentStatsDao.getDeploymentStats();
        return deploymentStats;
    }

    /**
     * Retrieves interval-based api usage stats within a specified time frame or per hour by default.
     * @see ApiUsageStats
     * @see ApiUsageStatsDao.RequestInterval
     * @param request HttpServletRequest with optional query parameter 'interval' which should be a
     *                string representation of a RequestInterval value (e.g 'HOUR').
     * @return ApiUsageStats
     */
    private ApiUsageStats getApiUsageStats(HttpServletRequest request)
    {
        ApiUsageStatsDao.RequestInterval requestInterval;
        try {
            requestInterval = ApiUsageStatsDao.RequestInterval.valueOf(request.getParameter("interval"));
        }
        catch (Exception ex) {
            logger.warn("Invalid interval parameter; defaulting to HOUR.");
            requestInterval = ApiUsageStatsDao.RequestInterval.HOUR;
        }

        return apiUsageStatsDao.getApiUsageStats(getBeginTimestamp(request), getEndTimestamp(request), requestInterval);
    }

    /**
     * Returns GeocodeUsageStats from `sinceDays` ago to now.
     * @param request: HttpServletRequest, Optional query parameter: sinceDays (int)
     * @return GeocodeStats
     */
    private GeocodeStats getGeocodeUsageStats(HttpServletRequest request)
    {
        GeocodeStatsDao gsd = new GeocodeStatsDao();
        Integer sinceDays = null;
        try {
            sinceDays = Integer.parseInt(request.getParameter("sinceDays"));
        }
        catch (NumberFormatException ex) {}
        return gsd.getGeocodeStats(getBeginTimestamp(request), getEndTimestamp(request));
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
        JobProcessDao jpd = new JobProcessDao();
        Timestamp from = getBeginTimestamp(request);
        Timestamp to = getEndTimestamp(request);
        statuses = jpd.getJobStatusesByConditions(Arrays.asList(JobProcessStatus.Condition.values()), null, from, to);
        for (JobProcessStatus jobProcessStatus : statuses) {
            statusViews.add(new JobProcessStatusView(jobProcessStatus));
        }
        return statusViews;
    }

    private Map<Integer, ApiUserStats> getApiUserStats(HttpServletRequest request)
    {
        return apiUserStatsDao.getRequestCounts(getBeginTimestamp(request), getEndTimestamp(request));
    }

    private List<ExceptionInfo> getExceptionStats(HttpServletRequest request)
    {
        ExceptionInfoDao exceptionInfoDao = new ExceptionInfoDao();
        return exceptionInfoDao.getExceptionInfoList(true);
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
        ExceptionInfoDao exceptionInfoDao = new ExceptionInfoDao();
        int update = exceptionInfoDao.hideExceptionInfo(id);
        return (update > 0) ? new GenericResponse(true, "Exception hidden")
                            : new GenericResponse(false, "Failed to hide exception!");
    }
}
