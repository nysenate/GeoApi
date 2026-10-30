package gov.nysenate.sage.controller.api.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Adds security-related HTTP response headers to every request.
 */
@Component
public class SecurityHeadersFilter implements Filter {
    @Value("${security.csp.frame-ancestors:'self' https://nysenate.gov https://*.nysenate.gov}")
    private String cspFrameAncestors;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        // Prevent MIME-type sniffing
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");
        // Restrict framing via CSP frame-ancestors property. Plugins and <base> are never used,
        // and blocking them removes two ways injected markup could load or redirect scripts.
        httpResponse.setHeader("Content-Security-Policy", "frame-ancestors " + cspFrameAncestors.trim()
                + "; object-src 'none'; base-uri 'self'");
        // Disable the legacy XSS auditor; it's deprecated and has itself caused vulnerabilities (CSP is the real defense)
        httpResponse.setHeader("X-XSS-Protection", "0");
        // Don't send the full URL as referer when navigating to a different origin
        httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        // Session-backed data (admin stats, api keys, job lists and result files) must not
        // outlive the session in the browser's cache, e.g. for the next user of a shared computer.
        if (isSessionBacked((HttpServletRequest) request)) {
            httpResponse.setHeader("Cache-Control", "no-store");
        }
        chain.doFilter(request, response);
    }

    private static boolean isSessionBacked(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return path.startsWith("/admin/") || path.startsWith("/job/") || path.equals("/globals");
    }
}
