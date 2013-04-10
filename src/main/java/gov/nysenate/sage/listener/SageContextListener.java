package gov.nysenate.sage.listener;

import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.util.FormatUtil;
import org.apache.log4j.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * SageContextListener is used to call initialization methods when the context is (re)deployed and
 * perform cleanup when the context is being shut down.
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

        /** Build instances and set the init attribute to true if succeeded */
        boolean buildStatus = ApplicationFactory.buildInstances();
        logger.info("Bootstrapped using ApplicationFactory: " + buildStatus);
        sce.getServletContext().setAttribute("init", buildStatus);
    }

    /**
     * Shutting down context
     * This method is invoked when the Servlet Context (the Web application) is undeployed or
     * Application Server shuts down.
    */
    public void contextDestroyed(ServletContextEvent sce)
    {
        logger.info("Closing data source");
        ApplicationFactory.getDataSource().close();
        ApplicationFactory.getTigerDataSource().close();
    }
}
