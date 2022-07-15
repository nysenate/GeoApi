package gov.nysenate.sage.controller.admin;

import gov.nysenate.sage.dao.model.admin.SqlAdminUserDao;
import gov.nysenate.sage.model.admin.AdminUser;
import gov.nysenate.sage.util.controller.ApiControllerUtil;
import gov.nysenate.sage.util.controller.ConstantUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.subject.WebSubject;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import static gov.nysenate.sage.util.controller.ApiControllerUtil.setAuthenticated;
import static gov.nysenate.sage.util.controller.ConstantUtil.*;

@Controller
@RequestMapping(value = ADMIN_REST_PATH)
public class AdminController
{
    private Logger logger = LoggerFactory.getLogger(AdminController.class);
    private SqlAdminUserDao sqlAdminUserDao;

    @Autowired
    @Qualifier("securityManager")
    protected DefaultWebSecurityManager securityManager;

    @Autowired
    public AdminController(SqlAdminUserDao sqlAdminUserDao) {
        this.sqlAdminUserDao = sqlAdminUserDao;
    }

    /**
     * Admin Login Api
     * ---------------------
     *
     * Attempt to login to the sage admin panel with the supplied credentials
     *
     * Usage:
     * (POST)    /admin/login
     *
     * PathParams
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param username String
     * @param password String
     * @throws IOException
     * @throws ServletException
     *
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public void adminLogin(HttpServletRequest request, HttpServletResponse response,
                           @RequestParam String username, @RequestParam String password)
            throws ServletException, IOException {

        String ipAddr= ApiControllerUtil.getIpAddress(request);
        if (sqlAdminUserDao.checkAdminUser(username, password)) {
            AdminUser dbAdmin = sqlAdminUserDao.getAdminUser(username);

            WebSubject webSubject = createSubject(request, response);
            webSubject.login(new UsernamePasswordToken(username, dbAdmin.getPassword() , ipAddr));
            Session session = webSubject.getSession(true);
            session.setAttribute( ADMIN_USERNAME_ATTR, username);
            session.setAttribute( AUTHENTICATED, true);

            logger.info("Granted admin access to " + username);
            boolean loggedIn = webSubject.hasRole("ADMIN");
            Object auth = webSubject.getSession().getAttribute(AUTHENTICATED);
            response.sendRedirect(request.getContextPath() + "/admin/home");


        }
        else {
            request.setAttribute("errorMessage", "Invalid admin credentials!");
            request.getRequestDispatcher(ADMIN_LOGIN_JSP).forward(request, response);
        }
    }

    /**
     * Admin Logout Api
     * ---------------------
     *
     * Logs the admin user out of the sage admin panel
     *
     * Usage:
     * (GET)    /admin/logout
     *
     * PathParams
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws ServletException
     * @throws IOException
     *
     */
    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public void adminLogout(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        session.setAttribute(ADMIN_USERNAME_ATTR, null);

        WebSubject webSubject = createSubject(request, response);
        Session shiroSession = webSubject.getSession();
        shiroSession.setAttribute(ADMIN_USERNAME_ATTR, null);
        shiroSession.setAttribute(AUTHENTICATED, false);
        webSubject.logout();
        request.getRequestDispatcher(ADMIN_LOGIN_JSP).forward(request, response);
    }


    protected WebSubject createSubject(ServletRequest request, ServletResponse response) {
        return new WebSubject.Builder(securityManager, request, response).buildWebSubject();
    }

}
