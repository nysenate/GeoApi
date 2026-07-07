package gov.nysenate.sage.controller.admin;

import gov.nysenate.sage.client.response.base.GenericResponse;
import gov.nysenate.sage.dao.model.admin.SqlAdminUserDao;
import gov.nysenate.sage.model.admin.AdminUser;
import gov.nysenate.sage.util.controller.ApiControllerUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

import static gov.nysenate.sage.util.controller.ApiControllerUtil.setAuthenticated;
import static gov.nysenate.sage.util.controller.ConstantUtil.ADMIN_REST_PATH;
import static gov.nysenate.sage.util.controller.ConstantUtil.ADMIN_USERNAME_ATTR;

@RestController
@RequestMapping(value = ADMIN_REST_PATH)
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private static final String ADMIN_MAIN_PATH = "/admin";
    private final SqlAdminUserDao sqlAdminUserDao;

    @Autowired
    public AdminController(SqlAdminUserDao sqlAdminUserDao) {
        this.sqlAdminUserDao = sqlAdminUserDao;
    }

    /**
     * Admin Login Api
     * ---------------------
     * Attempt to log in to the sage admin panel with the supplied credentials.
     * The React login page handles the response client-side.
     * Usage:
     * (POST)    /admin/login
     *
     */
    @PostMapping(value = "/login")
    public GenericResponse adminLogin(HttpServletRequest request,
                                      @RequestParam String username, @RequestParam String password) {
        String ipAddr = ApiControllerUtil.getIpAddress(request);
        if (!sqlAdminUserDao.checkAdminUser(username, password)) {
            return new GenericResponse(false, "Invalid admin credentials!");
        }
        AdminUser dbAdmin = sqlAdminUserDao.getAdminUser(username);
        try {
            SecurityUtils.getSubject().login(new UsernamePasswordToken(username, dbAdmin.getPassword(), ipAddr));
        } catch (AuthenticationException ex) {
            logger.warn("Admin login blocked for {}: {}", username, ex.getMessage());
            return new GenericResponse(false, "Login is not permitted from your location.");
        }
        setAuthenticated(request, true, username);
        return new GenericResponse(true, null);
    }

    /**
     * Admin Logout Api
     * ---------------------
     * Logs the admin user out of the sage admin panel and returns to the React
     * login page
     * Usage:
     * (GET)    /admin/logout
     *
     */
    @GetMapping(value = "/logout")
    public void adminLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        session.setAttribute(ADMIN_USERNAME_ATTR, null);
        SecurityUtils.getSubject().logout();
        response.sendRedirect(request.getContextPath() + ADMIN_MAIN_PATH);
    }
}
