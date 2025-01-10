package gov.nysenate.sage.controller.admin;

import gov.nysenate.sage.dao.model.admin.SqlAdminUserDao;
import gov.nysenate.sage.model.admin.AdminUser;
import gov.nysenate.sage.util.controller.ApiControllerUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import static gov.nysenate.sage.util.controller.ApiControllerUtil.setAuthenticated;
import static gov.nysenate.sage.util.controller.ConstantUtil.ADMIN_REST_PATH;
import static gov.nysenate.sage.util.controller.ConstantUtil.ADMIN_USERNAME_ATTR;

@Controller
@RequestMapping(value = ADMIN_REST_PATH)
public class AdminController {
    private static final String ADMIN_LOGIN_JSP = "/WEB-INF/views/adminlogin.jsp", ADMIN_MAIN_PATH = "/admin";
    private final SqlAdminUserDao sqlAdminUserDao;

    @Autowired
    public AdminController(SqlAdminUserDao sqlAdminUserDao) {
        this.sqlAdminUserDao = sqlAdminUserDao;
    }

    /**
     * Admin Login Api
     * ---------------------
     * Attempt to log in to the sage admin panel with the supplied credentials
     * Usage:
     * (POST)    /admin/login
     *
     */
    @PostMapping(value = "/login")
    public void adminLogin(HttpServletRequest request, HttpServletResponse response,
                           @RequestParam String username, @RequestParam String password)
            throws ServletException, IOException {

        String ipAddr= ApiControllerUtil.getIpAddress(request);
        if (sqlAdminUserDao.checkAdminUser(username, password)) {
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
     * Admin Logout Api
     * ---------------------
     * Logs the admin user out of the sage admin panel
     * Usage:
     * (GET)    /admin/logout
     *
     */
    @GetMapping(value = "/logout")
    public void adminLogout(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        session.setAttribute(ADMIN_USERNAME_ATTR, null);
        SecurityUtils.getSubject().logout();
        request.getRequestDispatcher(ADMIN_LOGIN_JSP).forward(request, response);
    }
}
