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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
public class AdminApiController extends BaseAdminController
{
    private Logger logger = LogManager.getLogger(AdminApiController.class);

    private ApiRequestLogger apiRequestLogger;
    private ApiUserStatsDao apiUserStatsDao;
    private ApiUsageStatsDao apiUsageStatsDao;
    private DeploymentStatsDao deploymentStatsDao;

    private static String ADMIN_LOGIN_JSP = "/WEB-INF/views/adminlogin.jsp";
    private static String ADMIN_MAIN_JSP = "/WEB-INF/views/adminmain.jsp";

    @Autowired
    public AdminApiController(ApiRequestLogger apiRequestLogger, ApiUserStatsDao apiUserStatsDao,
                              ApiUsageStatsDao apiUsageStatsDao, DeploymentStatsDao deploymentStatsDao) {
        this.apiRequestLogger = apiRequestLogger;
        this.apiUserStatsDao = apiUserStatsDao;
        this.apiUsageStatsDao = apiUsageStatsDao;
        this.deploymentStatsDao = deploymentStatsDao;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {}

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        Object adminResponse = null;
        if (isAuthenticated(request)) {
            String method = request.getPathInfo();
            if (method != null) {
                switch (method) {
                    case "/currentApiUsers" : {
                        adminResponse = getCurrentApiUsers(request);
                        break;
                    }
                    case "/currentJobUsers" : {
                        adminResponse = getCurrentJobUsers(request);
                        break;
                    }
                    case "/apiUserUsage" : {
                        adminResponse = getApiUserStats(request);
                        break;
                    }
                    case "/usage" : {
                        adminResponse = getApiUsageStats(request);
                        break;
                    }
                    case "/geocodeUsage" : {
                        adminResponse = getGeocodeUsageStats(request);
                        break;
                    }
                    case "/jobStatuses" : {
                        adminResponse = getJobProcessStatusList(request);
                        break;
                    }
                    case "/deployment" : {
                        adminResponse = getDeploymentStats(request);
                        break;
                    }
                    case "/exception" : {
                        adminResponse = getExceptionStats(request);
                        break;
                    }
                    default : {
                        adminResponse = new GenericResponse(false, "Invalid admin API request.");
                    }
                }
            }
        }
        else {
            adminResponse = new GenericResponse(false, "You must be logged in as an administrator to access this API.");
        }
        setAdminResponse(adminResponse, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        Object adminResponse = null;
        if (isAuthenticated(request)) {
            String method = request.getPathInfo();
            if (method != null) {
                switch (method) {
                    case "/createApiUser" : {
                        adminResponse = createApiUser(request);
                        break;
                    }
                    case "/deleteApiUser" : {
                        adminResponse = deleteApiUser(request);
                        break;
                    }
                    case "/createJobUser" : {
                        adminResponse = createJobUser(request);
                        break;
                    }
                    case "/deleteJobUser" : {
                        adminResponse = deleteJobUser(request);
                        break;
                    }
                    case "/hideException" : {
                        adminResponse = hideException(request);
                        break;
                    }
                    default : {
                        adminResponse = new GenericResponse(false, "Invalid admin API request.");
                    }
                }
            }
        }
        else {
            adminResponse = new GenericResponse(false, "You must be logged in as an administrator to access this API.");
        }
        setAdminResponse(adminResponse, response);
    }

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
