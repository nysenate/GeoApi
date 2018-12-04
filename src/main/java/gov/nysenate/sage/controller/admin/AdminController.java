package gov.nysenate.sage.controller.admin;

import gov.nysenate.sage.dao.logger.SqlApiRequestLogger;
import gov.nysenate.sage.dao.model.SqlAdminUserDao;
import gov.nysenate.sage.dao.stats.SqlApiUsageStatsDao;
import gov.nysenate.sage.dao.stats.SqlApiUserStatsDao;
import gov.nysenate.sage.dao.stats.SqlDeploymentStatsDao;
import gov.nysenate.sage.model.admin.AdminUser;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import gov.nysenate.sage.util.controller.ConstantUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import static gov.nysenate.sage.util.controller.ApiControllerUtil.setAuthenticated;
import static gov.nysenate.sage.util.controller.ConstantUtil.*;

@Controller
@RequestMapping(value = "admin")
public class AdminController
{
    private Logger logger = LoggerFactory.getLogger(AdminController.class);

    private SqlApiRequestLogger sqlApiRequestLogger;
    private SqlApiUserStatsDao sqlApiUserStatsDao;
    private SqlApiUsageStatsDao sqlApiUsageStatsDao;
    private SqlDeploymentStatsDao sqlDeploymentStatsDao;
    private SqlAdminUserDao sqlAdminUserDao;

    @Autowired
    public AdminController(SqlApiRequestLogger sqlApiRequestLogger, SqlApiUserStatsDao sqlApiUserStatsDao,
                           SqlApiUsageStatsDao sqlApiUsageStatsDao, SqlDeploymentStatsDao sqlDeploymentStatsDao,
                           SqlAdminUserDao sqlAdminUserDao) {
        this.sqlApiRequestLogger = sqlApiRequestLogger;
        this.sqlApiUserStatsDao = sqlApiUserStatsDao;
        this.sqlApiUsageStatsDao = sqlApiUsageStatsDao;
        this.sqlDeploymentStatsDao = sqlDeploymentStatsDao;
        this.sqlAdminUserDao = sqlAdminUserDao;
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
        String forwardedForIp = request.getHeader("x-forwarded-for");
        String ipAddr= forwardedForIp == null ? request.getRemoteAddr() : forwardedForIp;

        if (sqlAdminUserDao.checkAdminUser(username, password)) {
            logger.debug("Granted admin access to " + username);
            AdminUser dbAdmin = sqlAdminUserDao.getAdminUser(username);
            SecurityUtils.getSubject().login(new UsernamePasswordToken(username, dbAdmin.getPassword() , ipAddr));
            setAuthenticated(request, true, username);
            response.sendRedirect(request.getContextPath() + ADMIN_MAIN_PATH + "/home");
        }
        else {
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
        HttpSession session = request.getSession();
        session.setAttribute(ADMIN_USERNAME_ATTR, null);
        SecurityUtils.getSubject().logout();
        request.getRequestDispatcher(ADMIN_LOGIN_JSP).forward(request, response);
    }
}
