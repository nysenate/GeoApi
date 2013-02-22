package gov.nysenate.sage.factory;

import gov.nysenate.sage.provider.MapQuest;
import gov.nysenate.sage.adapter.StreetData;
import gov.nysenate.sage.adapter.YahooBoss;
import gov.nysenate.sage.provider.USPS;
import gov.nysenate.sage.listener.SageConfigurationListener;
import gov.nysenate.sage.service.ServiceProviders;
import gov.nysenate.sage.service.address.AddressService;
import gov.nysenate.sage.service.district.DistrictService;
import gov.nysenate.sage.service.geo.GeocodeService;
import gov.nysenate.sage.util.Config;
import gov.nysenate.sage.util.DB;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.apache.tomcat.jdbc.pool.DataSource;

/**
 * ApplicationFactory is responsible for instantiating all single-instance objects that are utilized
 * across the application and providing a single access point for them. By utilizing the ApplicationFactory
 * all classes that would typically be implemented as singletons can be instantiated like regular classes
 * which allows for unit testing.
 *
 * The buildInstances method must be called once when the application is starting up. However if only
 * unit tests are to be run, the buildTestInstances method should be called instead. While these two
 * methods may setup similar dependencies, it will allow for using different configurations and
 * implementations for running unit tests.
 *
 * @author Ash
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

    /** Service Providers */
    private ServiceProviders<AddressService> addressServiceProviders = new ServiceProviders<>();
    private ServiceProviders<DistrictService> districtServiceProviders = new ServiceProviders<>();
    private ServiceProviders<GeocodeService> geocodeServiceProviders = new ServiceProviders<>();

    /** Default values */
    private static String defaultPropertyFileName = "app.properties";
    private static String defaultTestPropertyFileName = "test.app.properties";

    /**
     * Public access call to buildProduction()
     * @return boolean - If true then build succeeded
     */
    public static boolean buildInstances()
    {
        return factoryInstance.buildProduction();
    }

    /**
     * Public access call to buildTesting()
     * @return boolean - If true then build succeeded
     */
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
            /** Setup application config */
            this.configurationListener = new SageConfigurationListener();
            this.config = new Config(defaultPropertyFileName, this.configurationListener);
            this.db = new DB(this.config);

            /** Setup service providers */
            addressServiceProviders.registerDefaultProvider("usps", new USPS());
            addressServiceProviders.registerDefaultProvider("mapquest", new MapQuest());

            geocodeServiceProviders.registerDefaultProvider("mapquest", new MapQuest());
            geocodeServiceProviders.registerDefaultProvider("yahooboss", new YahooBoss());

            districtServiceProviders.registerDefaultProvider("streetfile", new StreetData());

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

    /**
     * Similar to buildProduction() except this method should be called at the start of unit tests.     *
     * @return boolean  If true then build succeeded
     */
    private boolean buildTesting()
    {
        try
        {
            /** Setup test application config */
            this.configurationListener = new SageConfigurationListener();
            this.config = new Config(defaultTestPropertyFileName, this.configurationListener);
            this.db = new DB(this.config);

            /** Setup service providers */


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

    /** Accessor Functions */

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

    public static ServiceProviders<AddressService> getAddressServiceProviders()
    {
        return factoryInstance.addressServiceProviders;
    }

    public static ServiceProviders<DistrictService> getDistrictServiceProviders()
    {
        return factoryInstance.districtServiceProviders;
    }

    public static ServiceProviders<GeocodeService> getGeoCodeServiceProviders()
    {
        return factoryInstance.geocodeServiceProviders;
    }

}