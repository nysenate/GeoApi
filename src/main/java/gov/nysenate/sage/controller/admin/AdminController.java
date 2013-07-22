package gov.nysenate.sage.controller.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nysenate.sage.client.response.base.BaseResponse;
import gov.nysenate.sage.client.response.base.GenericResponse;
import gov.nysenate.sage.controller.api.BaseApiController;
import gov.nysenate.sage.dao.logger.ApiRequestLogger;
import gov.nysenate.sage.dao.model.ApiUserDao;
import gov.nysenate.sage.dao.model.JobUserDao;
import gov.nysenate.sage.dao.stats.ApiUsageStatsDao;
import gov.nysenate.sage.dao.stats.ApiUserStatsDao;
import gov.nysenate.sage.dao.stats.DeploymentStatsDao;
import gov.nysenate.sage.model.api.ApiUser;
import gov.nysenate.sage.model.job.JobUser;
import gov.nysenate.sage.model.stats.ApiUsageStats;
import gov.nysenate.sage.model.stats.ApiUserStats;
import gov.nysenate.sage.model.stats.DeploymentStats;
import gov.nysenate.sage.model.stats.ExceptionStats;
import gov.nysenate.sage.util.FormatUtil;
import gov.nysenate.sage.util.auth.ApiUserAuth;
import gov.nysenate.sage.util.auth.JobUserAuth;
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AdminController extends BaseAdminController
{
    private Logger logger = Logger.getLogger(AdminController.class);

    private ApiRequestLogger apiRequestLogger;
    private ApiUserStatsDao apiUserStatsDao;
    private ApiUsageStatsDao apiUsageStatsDao;
    private DeploymentStatsDao deploymentStatsDao;

    private static String ADMIN_JSP = "/WEB-INF/views/admin.jsp";

    @Override
    public void init(ServletConfig config) throws ServletException {
        apiRequestLogger = new ApiRequestLogger();
        apiUserStatsDao = new ApiUserStatsDao();
        apiUsageStatsDao = new ApiUsageStatsDao();
        deploymentStatsDao = new DeploymentStatsDao();
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        Object adminResponse = "NOT SET";
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
                case "/usage" : {
                    adminResponse = getApiUsageStats(request);
                    break;
                }
                case "/deployment" : {
                    adminResponse = getDeploymentStats(request);
                    break;
                }
                case "/exception" : {

                    break;
                }
                default : {

                }
            }
        }
        setAdminResponse(adminResponse, response);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        request.getRequestDispatcher(ADMIN_JSP).forward(request, response);
    }

    /**
     * Retrieves all registered Api Users.
     * @param request
     * @return
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
     * @return
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
     * @return
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
     * @return
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
     * @return
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
     *
     * @param request
     * @return
     */
    private DeploymentStats getDeploymentStats(HttpServletRequest request)
    {
        DeploymentStats deploymentStats = deploymentStatsDao.getDeploymentStats();
        return deploymentStats;
    }

    /**
     *
     * @param request
     * @return
     */
    private ApiUsageStats getApiUsageStats(HttpServletRequest request)
    {
        Timestamp from, to;
        try {
            from = new Timestamp(Long.parseLong(request.getParameter("from")));
            to = new Timestamp(Long.parseLong(request.getParameter("to")));
        }
        catch (Exception ex) {
            logger.warn("Invalid from/to parameters.");
            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            to = new Timestamp(c.getTimeInMillis());
            c.add(Calendar.MONTH, -1);
            from = new Timestamp(c.getTimeInMillis());
        }

        ApiUsageStatsDao.RequestInterval requestInterval;
        try {
            requestInterval = ApiUsageStatsDao.RequestInterval.valueOf(request.getParameter("interval"));
        }
        catch (Exception ex) {
            logger.warn("Invalid interval parameter; defaulting to HOUR.");
            requestInterval = ApiUsageStatsDao.RequestInterval.HOUR;
        }

        return apiUsageStatsDao.getApiUsageStats(from, to, requestInterval);
    }

    private ExceptionStats getExceptionStats(HttpServletRequest request)
    {
        return null;
    }
}