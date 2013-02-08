package gov.nysenate.sage.factory;

import gov.nysenate.sage.listener.SageConfigurationListener;
import gov.nysenate.sage.util.Config;
import gov.nysenate.sage.util.DB;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

/**
 * ApplicationFactory is responsible for instantiating all single-instance objects that are utilized
 * across the application and providing a single access point for them. By utilizing the ApplicationFactory
 * all classes that would typically be implemented as singletons can be instantiated like regular classes
 * which allows for unit testing.
 *
 * @author ash
 */
public class ApplicationFactory
{
    private static final Logger logger = Logger.getLogger(ApplicationFactory.class);

    /** Static factory instance */
    private static final ApplicationFactory factoryInstance = new ApplicationFactory();

    /** Dependency instances */
    private SageConfigurationListener configurationListener;
    private Config config;
    private DB db;

    /** Default values */
    private static String defaultPropertyFileName = "app.properties";
    private static String defaultTestPropertyFileName = "test_app.properties";

    /**
     * Public access call to buildProduction()
     * @return boolean - If true then build succeeded
     */
    public static boolean buildInstances()
    {
        return factoryInstance.buildProduction();
    }

    public static boolean buildTestInstances()
    {
        return factoryInstance.buildTesting();
    }

    /**
     * The buildProduction() method will construct all the objects and their necessary dependencies that are
     * needed in the application scope..
     *
     * @return boolean  If true then build succeeded
     */
    private boolean buildProduction()
    {
        try
        {
            this.configurationListener = new SageConfigurationListener();
            this.config = new Config(defaultPropertyFileName, this.configurationListener);
            this.db = new DB(this.config);
            return true;
        }
        catch (ConfigurationException ce)
        {
            logger.fatal("Failed to load configuration file " + defaultPropertyFileName);
            logger.fatal(ce.getMessage());
        }
        catch (Exception ex)
        {
            logger.fatal("An exception occurred while building dependencies");
            logger.fatal(ex.getMessage());
        }
        return false;
    }

    private boolean buildTesting()
    {
        try
        {
            this.configurationListener = new SageConfigurationListener();
            this.config = new Config(defaultTestPropertyFileName, this.configurationListener);
            this.db = new DB(this.config);
            return true;
        }
        catch (ConfigurationException ce)
        {
            logger.fatal("Failed to load configuration file " + defaultTestPropertyFileName);
            logger.fatal(ce.getMessage());
        }
        catch (Exception ex)
        {
            logger.fatal("An exception occurred while building dependencies");
            logger.fatal(ex.getMessage());
        }
        return false;
    }

    public static Config getConfig()
    {
        return factoryInstance.config;
    }

    public static SageConfigurationListener getConfigurationListener()
    {
        return factoryInstance.configurationListener;
    }

    public static DataSource getDataSource()
    {
        return factoryInstance.db.getDataSource();
    }

}