package gov.nysenate.sage.controller.api.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Adds security-related HTTP response headers to every request.
 *
 * Not included here: Content-Security-Policy requires careful tuning due to
 * AngularJS eval usage, inline scripts, and dynamic Google Maps resource loading.
 * Add it separately once those constraints are understood.
 */
@Component
public class SecurityHeadersFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        // Prevent MIME-type sniffing
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");
        // Restrict framing to same origin and nysenate.gov (X-Frame-Options can't express multi-origin;
        // frame-ancestors in CSP supersedes it in all modern browsers)
        httpResponse.setHeader("Content-Security-Policy", "frame-ancestors 'self' https://nysenate.gov https://*.nysenate.gov");
        // Legacy XSS filter hint for older browsers
        httpResponse.setHeader("X-XSS-Protection", "1; mode=block");
        // Don't send the full URL as referer when navigating to a different origin
        httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        chain.doFilter(request, response);
    }
}
