package gov.nysenate.sage.controller.admin;

import gov.nysenate.sage.controller.api.BaseApiController;
import gov.nysenate.sage.dao.logger.ApiRequestLogger;
import gov.nysenate.sage.dao.stats.ApiUserStatsDao;
import gov.nysenate.sage.dao.stats.DeploymentStatsDao;
import gov.nysenate.sage.model.stats.DeploymentStats;
import gov.nysenate.sage.util.FormatUtil;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AdminController extends BaseApiController
{
    private ApiRequestLogger apiRequestLogger;
    private ApiUserStatsDao apiUserStatsDao;
    private DeploymentStatsDao deploymentStatsDao;

    @Override
    public void init(ServletConfig config) throws ServletException {
        apiRequestLogger = new ApiRequestLogger();
        apiUserStatsDao = new ApiUserStatsDao();
        deploymentStatsDao = new DeploymentStatsDao();
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        DeploymentStats deploymentStats = deploymentStatsDao.getDeploymentStats();
        request.setAttribute("lastDeployed", deploymentStats.getLastDeploymentTime().getTime());
        request.setAttribute("latestUptime", deploymentStats.getLatestUptime());
        request.setAttribute("latestRequestsSince", deploymentStats.getRequestsSinceLatest());
        request.getRequestDispatcher("/admin.jsp").forward(request, response);
    }
}
