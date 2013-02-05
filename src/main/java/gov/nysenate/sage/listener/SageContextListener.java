package gov.nysenate.sage.listener;

import gov.nysenate.sage.factory.ApplicationFactory;
import org.apache.log4j.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * SageContextListener is used to call initialization methods when the context is (re)deployed and
 * perform cleanup when the context is being shut down. *
 *
 */
@WebListener()
public class SageContextListener implements ServletContextListener {

    public Logger logger = Logger.getLogger(this.getClass());

    public SageContextListener() {}

    /**
     * Starting up context
     * This method is invoked when the Servlet Context (the Web application) is (re)deployed.
     */
    public void contextInitialized(ServletContextEvent sce)
    {
        logger.info("Servlet Context Listener started.");
        ApplicationFactory.build();
    }

    /**
     * Shutting down context
     * This method is invoked when the Servlet Context (the Web application) is undeployed or
     * Application Server shuts down.
    */
    public void contextDestroyed(ServletContextEvent sce) {
        /** Shut-down tasks */
        logger.info("Servlet Context Listener stopping.");
    }
}
