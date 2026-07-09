package gov.nysenate.sage.controller.api.filter;

import jakarta.servlet.*;
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
        // Restrict framing via CSP frame-ancestors property
        httpResponse.setHeader("Content-Security-Policy", "frame-ancestors " + cspFrameAncestors.trim());
        // Disable the legacy XSS auditor; it's deprecated and has itself caused vulnerabilities (CSP is the real defense)
        httpResponse.setHeader("X-XSS-Protection", "0");
        // Don't send the full URL as referer when navigating to a different origin
        httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        chain.doFilter(request, response);
    }
}
