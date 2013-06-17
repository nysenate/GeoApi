package gov.nysenate.sage.listener;

import gov.nysenate.sage.dao.logger.DeploymentLogger;
import gov.nysenate.sage.factory.ApplicationFactory;
import org.apache.log4j.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.sql.Timestamp;
import java.util.Date;

/**
 * SageContextListener is used to call initialization methods when the context is (re)deployed and
 * perform cleanup when the context is being shut down.
 */
@WebListener()
public class SageContextListener implements ServletContextListener {

    public Logger logger = Logger.getLogger(this.getClass());
    public DeploymentLogger deploymentLogger;

    public SageContextListener() {}

    /**
     * Starting up context
     * This method is invoked when the Servlet Context (the Web application) is (re)deployed.
     */
    public void contextInitialized(ServletContextEvent sce)
    {
        logger.info("Servlet Context Listener started.");

        /** Build instances, initialize cache, and set the init attribute to true if succeeded */
        boolean buildStatus = ApplicationFactory.bootstrap();
        if (buildStatus && ApplicationFactory.getConfig() != null) {
            if (Boolean.parseBoolean(ApplicationFactory.getConfig().getValue("init.caches"))) {
                logger.info("Initializing caches in memory..");
                ApplicationFactory.initializeCache();
            }

            deploymentLogger = new DeploymentLogger();
            Integer deploymentId = deploymentLogger.logDeploymentStatus(true, -1, new Timestamp(new Date().getTime()));
            logger.info("Bootstrapped using ApplicationFactory: " + buildStatus);
            sce.getServletContext().setAttribute("deploymentId", deploymentId);
        }
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
        Integer deploymentId = (Integer) sce.getServletContext().getAttribute("deploymentId");
        deploymentLogger.logDeploymentStatus(false, deploymentId, new Timestamp(new Date().getTime()));
        boolean status = ApplicationFactory.close();
    }
}
