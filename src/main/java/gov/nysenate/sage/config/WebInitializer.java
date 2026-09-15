package gov.nysenate.sage.config;

import gov.nysenate.sage.util.controller.ConstantUtil;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;
import java.util.EnumSet;

import static jakarta.servlet.DispatcherType.*;

/**
 * Java based Spring configuration. This implementation is responsible for creating
 * the web Spring context and to setup the necessary servlets and filters.
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
        /** The only Spring context. The filter proxies below refresh it before the dispatcher servlet starts,
         * so the servlet context must be set here for the active profile in web.xml to take effect. */
        AnnotationConfigWebApplicationContext dispatcherContext = new AnnotationConfigWebApplicationContext();
        dispatcherContext.setServletContext(servletContext);
        dispatcherContext.register(WebApplicationConfig.class);

        /** Register the dispatcher servlet which basically serves as the front controller for Spring.
         * The servlet has to be mapped to the root path "/". */
        ServletRegistration.Dynamic dispatcher;
        dispatcher = servletContext.addServlet(DISPATCHER_SERVLET_NAME, new DispatcherServlet(dispatcherContext));
        dispatcher.setLoadOnStartup(1);
        dispatcher.addMapping("/");
        dispatcher.setAsyncSupported(true);

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

