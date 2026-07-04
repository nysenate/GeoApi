package gov.nysenate.sage.controller.interceptor;

import gov.nysenate.sage.config.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Populates the request attributes that the embedded map JSP view ({@code maps}) needs to render:
 * the USPS AMS UI url and the Google Maps script url (with the API key appended when one is
 * configured). The React front-end gets these values from {@code /globals} instead.
 */
@Component
public class PageSetupInterceptor implements HandlerInterceptor {
    private final Environment env;

    @Autowired
    public PageSetupInterceptor(Environment env) {
        this.env = env;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute("amsUrl", env.getUspsAmsUiUrl());

        String googleMapsUrl = env.getGoogleMapsUrl();
        String googleMapsKey = env.getGoogleMapsKey();
        if (googleMapsKey != null && !googleMapsKey.isEmpty()) {
            googleMapsUrl = googleMapsUrl + "&key=" + googleMapsKey;
        }
        request.setAttribute("googleMapsUrl", googleMapsUrl);
        return true;
    }
}
