package gov.nysenate.sage.controller.ui;

import gov.nysenate.sage.config.Environment;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class AngularAppCtrl {
    private static final Logger logger = LoggerFactory.getLogger(AngularAppCtrl.class);

    private Environment environment;

    @Autowired
    public AngularAppCtrl(Environment environment) {
        this.environment = environment;
    }

    @RequestMapping({"/"})
    public String home(HttpServletRequest request) {
        String forwardedForIp = request.getHeader("x-forwarded-for");
        String ipAddr= forwardedForIp == null ? request.getRemoteAddr() : forwardedForIp;
        return "index";
    }

    @RequestMapping({"/admin"})
    public String adminLogin(HttpServletRequest request) {
        String forwardedForIp = request.getHeader("x-forwarded-for");
        String ipAddr= forwardedForIp == null ? request.getRemoteAddr() : forwardedForIp;
        return "adminlogin";
    }

    @RequestMapping({"/admin/home"})
    public String adminHome(HttpServletRequest request) {
        String forwardedForIp = request.getHeader("x-forwarded-for");
        String ipAddr= forwardedForIp == null ? request.getRemoteAddr() : forwardedForIp;
        return "adminmain";
    }

    @RequestMapping({"/job"})
    public String jobLogin(HttpServletRequest request) {
        String forwardedForIp = request.getHeader("x-forwarded-for");
        String ipAddr= forwardedForIp == null ? request.getRemoteAddr() : forwardedForIp;
        return "joblogin";
    }

    @RequestMapping({"/job/home"})
    public String jobHome(HttpServletRequest request) {
        String forwardedForIp = request.getHeader("x-forwarded-for");
        String ipAddr= forwardedForIp == null ? request.getRemoteAddr() : forwardedForIp;
        return "jobmain";
    }
}
