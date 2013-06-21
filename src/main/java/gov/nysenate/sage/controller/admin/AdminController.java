package gov.nysenate.sage.controller.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nysenate.sage.controller.api.BaseApiController;
import gov.nysenate.sage.dao.logger.ApiRequestLogger;
import gov.nysenate.sage.dao.stats.ApiUsageStatsDao;
import gov.nysenate.sage.dao.stats.ApiUserStatsDao;
import gov.nysenate.sage.dao.stats.DeploymentStatsDao;
import gov.nysenate.sage.model.stats.ApiUsageStats;
import gov.nysenate.sage.model.stats.ApiUserStats;
import gov.nysenate.sage.model.stats.DeploymentStats;
import gov.nysenate.sage.util.FormatUtil;
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

public class AdminController extends BaseAdminController
{
    private Logger logger = Logger.getLogger(AdminController.class);

    private ApiRequestLogger apiRequestLogger;
    private ApiUserStatsDao apiUserStatsDao;
    private ApiUsageStatsDao apiUsageStatsDao;
    private DeploymentStatsDao deploymentStatsDao;

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
                case "/usage" : {
                    adminResponse = getApiUsageStats(request);
                    break;
                }
                case "/deployment" : {
                    adminResponse = getDeploymentStats(request);
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
        request.getRequestDispatcher("/admin.jsp").forward(request, response);
    }

    private DeploymentStats getDeploymentStats(HttpServletRequest request) {
        DeploymentStats deploymentStats = deploymentStatsDao.getDeploymentStats();
        return deploymentStats;
    }

    private ApiUsageStats getApiUsageStats(HttpServletRequest request) {
        Timestamp from, to;
        logger.info("from: " + request.getParameter("from"));
        logger.info("to: " + request.getParameter("to"));
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
}
