package gov.nysenate.sage.listener;

/**
 * SageContextListener is used to call initialization methods when the context is (re)deployed and
 * perform cleanup when the context is being shut down.
 */
//@WebListener()
//@Component
//public class SageContextListener extends BaseDao implements ServletContextListener {
//
//    public Logger logger = LoggerFactory.getLogger(this.getClass());
//    private DeploymentLogger deploymentLogger;
//    private ExceptionLogger exceptionLogger;
//
//    @Autowired
//    public SageContextListener(DeploymentLogger deploymentLogger, ExceptionLogger exceptionLogger) {
//        this.deploymentLogger = deploymentLogger;
//        this.exceptionLogger = exceptionLogger;
//    }
//
//    /**
//     * Starting up context
//     * This method is invoked when the Servlet Context (the Web application) is (re)deployed.
//     */
//    public void contextInitialized(ServletContextEvent sce) throws RuntimeException
//    {
//        logger.info("Servlet Context Listener started.");
//
//        /** Build instances, initialize cache, and set the init attribute to true if succeeded */
//        sce.getServletContext().setAttribute("init", true);
//        if ( getConfig() != null) {
//            Integer deploymentId = deploymentLogger.logDeploymentStatus(true, -1, new Timestamp(new Date().getTime()));
//            logger.info("Successfully logged deployment" + deploymentId);
//            sce.getServletContext().setAttribute("deploymentId", deploymentId);
//        }
//        else {
//            RuntimeException appFactoryBuildEx = new RuntimeException("Could not build deploy");
//            exceptionLogger.logException(appFactoryBuildEx, new Timestamp(new Date().getTime()),null);
//            throw appFactoryBuildEx;
//        }
//    }
//
//    /**
//     * Shutting down context
//     * This method is invoked when the Servlet Context (the Web application) is undeployed or
//     * Application Server shuts down.
//    */
//    public void contextDestroyed(ServletContextEvent sce)
//    {
//        logger.info("Closing data source");
//        Integer deploymentId = (Integer) sce.getServletContext().getAttribute("deploymentId");
//        deploymentLogger.logDeploymentStatus(false, deploymentId, new Timestamp(new Date().getTime()));
//        close();
//    }
//}