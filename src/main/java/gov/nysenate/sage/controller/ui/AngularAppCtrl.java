package gov.nysenate.sage.controller.ui;

import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.util.controller.ApiControllerUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * Simple entry point to the front-end. Returns the main angular-js driven web page which will handle
 * all the heavy lifting.
 */
@Controller
public class AngularAppCtrl {
    private final String ipWhitelist;

    @Autowired
    public AngularAppCtrl(Environment env) {
        ipWhitelist = env.getUserIpFilter();
    }

    @RequestMapping({"/"})
    public String home(HttpServletRequest request) {
        String ipAddr= ApiControllerUtil.getIpAddress(request);
        Subject subject = SecurityUtils.getSubject();
        // Senate staff and API users will be routed to the internal dev interface.
        if (subject.isPermitted("ui:view") || ipAddr.matches(ipWhitelist)) {
            return "index";
        }
        return "404";
    }

    @RequestMapping({"/admin"})
    public String adminLogin(HttpServletRequest request) {
        return "adminlogin";
    }

    @RequestMapping({"/admin/home"})
    public String adminHome(HttpServletRequest request) {
        return "adminmain";
    }

    @RequestMapping({"/job"})
    public String jobLogin(HttpServletRequest request) {
        return "joblogin";
    }

    @RequestMapping({"/job/home"})
    public String jobHome(HttpServletRequest request) {
        return "jobmain";
    }
}
