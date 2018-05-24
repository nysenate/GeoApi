package gov.nysenate.sage.controller.admin;

import gov.nysenate.sage.dao.logger.ApiRequestLogger;
import gov.nysenate.sage.dao.model.AdminUserDao;
import gov.nysenate.sage.dao.stats.ApiUsageStatsDao;
import gov.nysenate.sage.dao.stats.ApiUserStatsDao;
import gov.nysenate.sage.dao.stats.DeploymentStatsDao;
import gov.nysenate.sage.util.controller.ConstantUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static gov.nysenate.sage.util.controller.ApiControllerUtil.setAuthenticated;
import static gov.nysenate.sage.util.controller.ConstantUtil.ADMIN_LOGIN_JSP;
import static gov.nysenate.sage.util.controller.ConstantUtil.ADMIN_MAIN_JSP;
import static gov.nysenate.sage.util.controller.ConstantUtil.ADMIN_MAIN_PATH;

@Controller
@RequestMapping(value = ConstantUtil.REST_PATH + "admin")
public class AdminController
{
    private Logger logger = LogManager.getLogger(AdminController.class);

    private ApiRequestLogger apiRequestLogger;
    private ApiUserStatsDao apiUserStatsDao;
    private ApiUsageStatsDao apiUsageStatsDao;
    private DeploymentStatsDao deploymentStatsDao;

    @Autowired
    public AdminController(ApiRequestLogger apiRequestLogger, ApiUserStatsDao apiUserStatsDao,
                           ApiUsageStatsDao apiUsageStatsDao, DeploymentStatsDao deploymentStatsDao) {
        this.apiRequestLogger = apiRequestLogger;
        this.apiUserStatsDao = apiUserStatsDao;
        this.apiUsageStatsDao = apiUsageStatsDao;
        this.deploymentStatsDao = deploymentStatsDao;
    }

    /**
     * Attempt to log in using the supplied admin credentials.
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public void adminLogin(HttpServletRequest request, HttpServletResponse response,
                           @RequestParam String username, @RequestParam String password)
            throws ServletException, IOException {

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
    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public void adminLogout(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setAuthenticated(request, false, null);
        request.getRequestDispatcher(ADMIN_MAIN_JSP).forward(request, response);
    }
}
