package gov.nysenate.sage.controller.ui;

import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.util.controller.ApiControllerUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Entry point to the React front-end. Returns the main React app which handles
 * routing and rendering client-side.
 * The legacy Angular UI is no longer routed (the JSPs under WEB-INF/views are kept only
 * as reference while their pages are rebuilt in React, registered in WEB-INF/app/index.js).
 */
@Controller
public class ReactAppCtrl {
    private final Environment env;

    @Autowired
    public ReactAppCtrl(Environment env) {
        this.env = env;
    }

    @RequestMapping({"/", "/maps", "/usps", "/street", "/revgeo", "/admin", "/admin/home", "/job", "/job/home"})
    public String home(HttpServletRequest request) {
        // Senate staff and API users are routed to the internal dev interface;
        // everyone else gets the 404 page.
        if (isWhitelisted(request)) {
            return "forward:/static/dist/index.html";
        }
        return "404";
    }

    /**
     * Provides the config values the React front-end needs, replacing the request
     * attributes that PageSetupInterceptor sets for the JSP views.
     */
    @ResponseBody
    @RequestMapping("/globals")
    public GlobalsView globals(HttpServletRequest request) {
        Subject subject = SecurityUtils.getSubject();

        String googleMapsUrl = env.getGoogleMapsUrl();
        String googleMapsKey = env.getGoogleMapsKey();
        if (googleMapsKey != null && !googleMapsKey.isEmpty()) {
            googleMapsUrl = googleMapsUrl + "&key=" + googleMapsKey;
        }
        return new GlobalsView(env.getUspsAmsUiUrl(), googleMapsUrl, isWhitelisted(request),
                subject.hasRole("ADMIN"), subject.hasRole("JOB_USER"));
    }

    private boolean isWhitelisted(HttpServletRequest request) {
        String ipAddr = ApiControllerUtil.getIpAddress(request);
        return SecurityUtils.getSubject().isPermitted("ui:view") || ipAddr.matches(env.getUserIpFilter());
    }

    public record GlobalsView(String amsUrl, String googleMapsUrl, boolean isWhitelisted,
                              boolean isAdmin, boolean isJobUser) {}
}
