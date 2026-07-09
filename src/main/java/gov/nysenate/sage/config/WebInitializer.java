package gov.nysenate.sage.config;

import gov.nysenate.sage.util.controller.ConstantUtil;
import org.apache.catalina.filters.RemoteIpFilter;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;

import jakarta.servlet.FilterRegistration;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;
import java.util.EnumSet;

import static jakarta.servlet.DispatcherType.*;

/**
 * Java based Spring configuration. This implementation is responsible for creating
 * the root and web Spring contexts and to setup the necessary servlets and filters.
 * Note that this class is functionally equivalent to a web.xml configuration but we try
 * to do as much in Java to reduce complexity.
 */
public class WebInitializer implements WebApplicationInitializer
{
    protected static String DISPATCHER_SERVLET_NAME = "sage";

    /**
     * Bootstraps the web application. This method is invoked automatically by Spring.
     *
     * You might notice that all the filters are registered via a DelegatingFilterProxy. This
     * is simply because we instantiate all the filter implementations as Spring beans and we
     * want Spring to control the lifecycle of these beans. If they were declared without this
     * proxy filter, then the servlet container would be instantiating them and they wouldn't be
     * under the Spring context.
     *
     * @param servletContext ServletContext
     */
    @Override
    public void onStartup(ServletContext servletContext) {
        /** Create the root Spring application context. */
        AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();

        /** Manage the lifecycle of the root application context. */
        servletContext.addListener(new ContextLoaderListener(rootContext));

        /** The dispatcher servlet has it's own application context in which it can override
         * beans from the parent root context. */
        AnnotationConfigWebApplicationContext dispatcherContext = new AnnotationConfigWebApplicationContext();
        dispatcherContext.setServletContext(servletContext);
        dispatcherContext.setParent(rootContext);
        dispatcherContext.register(WebApplicationConfig.class);

        /** Register the dispatcher servlet which basically serves as the front controller for Spring.
         * The servlet has to be mapped to the root path "/". */
        ServletRegistration.Dynamic dispatcher;
        dispatcher = servletContext.addServlet(DISPATCHER_SERVLET_NAME, new DispatcherServlet(dispatcherContext));
        dispatcher.setLoadOnStartup(1);
        dispatcher.addMapping("/");
        dispatcher.setAsyncSupported(true);

        /** Resolve the real client IP from X-Forwarded-For, but only when the request arrives
         * via a trusted proxy. This must run first so every downstream filter, the Shiro realms,
         * and the controllers see the corrected request.getRemoteAddr(). Without this, a client
         * could spoof X-Forwarded-For to impersonate an internal/whitelisted address and bypass
         * the API-key requirement and the admin/job IP restrictions.
         *
         * With no override, RemoteIpFilter trusts only private/loopback ranges as proxies (its
         * default internalProxies) and ignores the header from any other peer. If the fronting
         * proxy is on a non-private address, set -Dsage.remoteip.internalProxies=<regex>. */
        FilterRegistration.Dynamic remoteIpFilter =
                servletContext.addFilter("remoteIpFilter", new RemoteIpFilter());
        String internalProxies = System.getProperty("sage.remoteip.internalProxies");
        if (internalProxies != null && !internalProxies.isBlank()) {
            remoteIpFilter.setInitParameter("internalProxies", internalProxies);
        }
        remoteIpFilter.addMappingForUrlPatterns(EnumSet.of(REQUEST, FORWARD, INCLUDE), false, "/*");

        /** Force UTF-8 on requests and responses. The JSP views declared this in their page
         * directive; without it, responses forwarded to static files (e.g. the React app's
         * index.html) default to the locale's ISO-8859-1 and garble non-ASCII characters. */
        CharacterEncodingFilter encodingFilter = new CharacterEncodingFilter("UTF-8", true, true);
        servletContext.addFilter("encodingFilter", encodingFilter)
                .addMappingForUrlPatterns(EnumSet.of(REQUEST, FORWARD, INCLUDE), false, "/*");

        /** Security response headers (X-Content-Type-Options, X-Frame-Options, etc.) */
        DelegatingFilterProxy securityHeadersFilter = new DelegatingFilterProxy("securityHeadersFilter", dispatcherContext);
        servletContext.addFilter("securityHeadersFilter", securityHeadersFilter)
                .addMappingForUrlPatterns(EnumSet.of(REQUEST, FORWARD, INCLUDE), false, "/*");

        /** Register Apache Shiro */
        DelegatingFilterProxy shiroFilter = new DelegatingFilterProxy("shiroFilter", dispatcherContext);
        shiroFilter.setTargetFilterLifecycle(true);
        servletContext.addFilter("shiroFilter", shiroFilter)
                .addMappingForUrlPatterns(EnumSet.of(REQUEST, FORWARD, INCLUDE), false, "/*");

        /** Api Key Authentication */
        DelegatingFilterProxy apiAuthFilter = new DelegatingFilterProxy("apiFilter", dispatcherContext);
        servletContext.addFilter("apiFilter", apiAuthFilter)
                .addMappingForUrlPatterns(EnumSet.of(REQUEST, FORWARD, INCLUDE), false, ConstantUtil.REST_PATH + "*");
    }
}

