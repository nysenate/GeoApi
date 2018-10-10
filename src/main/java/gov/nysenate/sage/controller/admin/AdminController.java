package gov.nysenate.sage.controller.admin;

import gov.nysenate.sage.dao.logger.ApiRequestLogger;
import gov.nysenate.sage.dao.model.AdminUserDao;
import gov.nysenate.sage.dao.stats.ApiUsageStatsDao;
import gov.nysenate.sage.dao.stats.ApiUserStatsDao;
import gov.nysenate.sage.dao.stats.DeploymentStatsDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AdminController extends BaseAdminController
{
    private Logger logger = LogManager.getLogger(AdminController.class);

    private ApiRequestLogger apiRequestLogger;
    private ApiUserStatsDao apiUserStatsDao;
    private ApiUsageStatsDao apiUsageStatsDao;
    private DeploymentStatsDao deploymentStatsDao;

    private static String ADMIN_LOGIN_PATH = "/admin/login";
    private static String ADMIN_LOGIN_JSP = "/WEB-INF/views/adminlogin.jsp";
    private static String ADMIN_MAIN_PATH = "/admin";
    private static String ADMIN_MAIN_JSP = "/WEB-INF/views/adminmain.jsp";

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
        String method = request.getPathInfo();
        if (method != null) {
            switch (method) {
                case "/login" : {
                    doLogin(request, response); break;
                }
            }
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        if (!isAuthenticated(request)) {
            request.getRequestDispatcher(ADMIN_LOGIN_JSP).forward(request, response);
        }
        else {
            String method = (request.getPathInfo() != null) ? request.getPathInfo() : "";
            switch (method) {
                case "/login": {
                    /** Fall through to logout */
                }
                case "/logout" : {
                    doLogout(request, response);
                    request.getRequestDispatcher(ADMIN_LOGIN_JSP).forward(request, response);
                    break;
                }
                default: {
                    request.getRequestDispatcher(ADMIN_MAIN_JSP).forward(request, response);
                }
            }
        }
    }

    /**
     * Attempt to log in using the supplied admin credentials.
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    public void doLogin(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        AdminUserDao adminUserDao = new AdminUserDao();
        if (adminUserDao.checkAdminUser(username, password)) {
            logger.debug("Granted admin access to " + username);
            setAuthenticated(request, true, username);
            response.sendRedirect(request.getContextPath() + ADMIN_MAIN_PATH);
        }
        else {
            setAuthenticated(request, false, null);
            request.setAttribute("errorMessage", "Invalid admin credentials!");
            request.getRequestDispatcher(ADMIN_LOGIN_JSP).forward(request, response);
        }
    }

    /**
     * Logs out the admin user from the current session.
     * @param request
     * @param response
     * @throws IOException
     */
    public void doLogout(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        setAuthenticated(request, false, null);
    }
}
