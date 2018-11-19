package gov.nysenate.sage.config;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import java.util.EnumSet;

import static javax.servlet.DispatcherType.*;

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
     * @throws ServletException
     */
    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        /** Create the root Spring application context. */
        AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
        rootContext.scan("gov.nysenate");

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

        /** Register the Apache Shiro web security filter using the 'shiroFilter' bean as the implementation. */
//        DelegatingFilterProxy shiroFilter = new DelegatingFilterProxy("shiroFilter", dispatcherContext);
//        shiroFilter.setTargetFilterLifecycle(true);
//        EnumSet<DispatcherType> dispatcherTypes = EnumSet.of(REQUEST, FORWARD, INCLUDE);
//        servletContext.addFilter("shiroFilter", shiroFilter)
//                .addMappingForUrlPatterns(dispatcherTypes, false, "/*");

        /** Registers the CommonAttributeFilter which sets request attributes for JSP pages. */
//        DelegatingFilterProxy commonAttributeFilter = new DelegatingFilterProxy("commonAttributeFilter", dispatcherContext);
//        servletContext.addFilter("commonAttributeFilter", commonAttributeFilter)
//                .addMappingForUrlPatterns(EnumSet.of(FORWARD), false, "/*");

//        /** Registers the restApiFilter which affects all REST API calls. */
//        DelegatingFilterProxy restApiFilter = new DelegatingFilterProxy("restApiFilter", dispatcherContext);
//        servletContext.addFilter("restApiFilter", restApiFilter)
//                .addMappingForUrlPatterns(EnumSet.of(REQUEST, FORWARD, INCLUDE), false, BaseRestApiCtrl.REST_PATH + "*");
    }
}

