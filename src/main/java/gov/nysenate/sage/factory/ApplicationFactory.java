package gov.nysenate.sage.factory;

import gov.nysenate.sage.listener.SageConfigurationListener;
import gov.nysenate.sage.util.Config;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;

/**
 * ApplicationFactory is responsible for instantiating all single-instance objects that are utilized
 * across the application and providing a single access point for them. By utilizing the ApplicationFactory
 * all classes that would typically be implemented as singletons can be created instantiated like
 * regular classes which allows for unit testing.
 *
 * @author ash
 */
public class ApplicationFactory {

    private static Logger logger = Logger.getLogger(ApplicationFactory.class);

    /** Static factory instance */
    private static ApplicationFactory factoryInstance = new ApplicationFactory();

    /** Dependency instances */
    private SageConfigurationListener configurationListener;
    private Config config;

    /** Default values */
    private static String defaultPropertyFileName = "app.properties";

    /**
     * The build() method will construct all the objects and their necessary dependencies that are
     * needed in the application scope. Classes that deal with configuration and other single instance
     * classes that are typically implemented as singletons are instantiated here.
     *
     * @return boolean  If true then build succeeded
     */
    public static boolean build()
    {
        try
        {
            factoryInstance.configurationListener = new SageConfigurationListener();
            factoryInstance.config = new Config(defaultPropertyFileName, factoryInstance.configurationListener);

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

    public static Config getConfig()
    {
        return factoryInstance.config;
    }

    public static SageConfigurationListener getConfigurationListener()
    {
        return factoryInstance.configurationListener;
    }

}