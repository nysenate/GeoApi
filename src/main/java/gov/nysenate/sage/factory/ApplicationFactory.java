package gov.nysenate.sage.factory;

import gov.nysenate.sage.dao.model.SenateDao;
import gov.nysenate.sage.dao.provider.DistrictShapefileDao;
import gov.nysenate.sage.listener.SageConfigurationListener;
import gov.nysenate.sage.provider.*;
import gov.nysenate.sage.service.address.AddressServiceProvider;
import gov.nysenate.sage.service.address.CityZipServiceProvider;
import gov.nysenate.sage.service.district.DistrictServiceProvider;
import gov.nysenate.sage.service.geo.GeocodeServiceProvider;
import gov.nysenate.sage.service.geo.RevGeocodeServiceProvider;
import gov.nysenate.sage.service.map.MapServiceProvider;
import gov.nysenate.sage.service.street.StreetLookupServiceProvider;
import gov.nysenate.sage.util.Config;
import gov.nysenate.sage.util.DB;
import gov.nysenate.services.model.Senator;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.apache.tomcat.jdbc.pool.DataSource;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;

/**
 * ApplicationFactory is responsible for instantiating all single-instance objects that are utilized
 * across the application and providing a single access point for them. By utilizing the ApplicationFactory
 * all classes that would typically be implemented as singletons can be instantiated like regular classes
 * which allows for unit testing.
 *
 * The bootstrap method must be called once when the application is starting up. However if only
 * unit tests are to be run, the bootstrapTest method should be called instead. While these two
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
    private ApplicationFactory() {}

    /** Dependency instances */
    private Config config;
    private DB baseDB;
    private DB tigerDB;

    /** Service Providers */
    private AddressServiceProvider addressServiceProvider;
    private DistrictServiceProvider districtServiceProvider;
    private GeocodeServiceProvider geocodeServiceProvider;
    private RevGeocodeServiceProvider revGeocodeServiceProvider;
    private MapServiceProvider mapServiceProvider;
    private StreetLookupServiceProvider streetLookupServiceProvider;
    private CityZipServiceProvider cityZipServiceProvider;

    /** Default values */
    private static String defaultPropertyFileName = "app.properties";
    private static String defaultTestPropertyFileName = "test.app.properties";

    /**
     * Sets up core application classes
     * @return boolean - If true then build succeeded
     */
    public static boolean bootstrap()
    {
        return factoryInstance.build(defaultPropertyFileName);
    }

    /**
     * Sets up core application classes for testing
     * @return boolean - If true then build succeeded
     */
    public static boolean bootstrapTest()
    {
        return factoryInstance.build(defaultTestPropertyFileName);
    }

    /**
     * Builds all the in-memory caches
     */
    public static void initializeCache()
    {
        factoryInstance.initCache();
    }

    /**
     * Closes all data connections
     * @return true if succeeded, false if exception was thrown
     */
    public static boolean close()
    {
        try {
            factoryInstance.baseDB.getDataSource().purge();
            factoryInstance.tigerDB.getDataSource().purge();
        }
        catch (Exception ex) {
            logger.error("Failed to purge data connections!", ex);
        }

        try {
            factoryInstance.baseDB.getDataSource().close();
            factoryInstance.tigerDB.getDataSource().close();
            return true;
        }
        catch (Exception ex) {
            logger.error("Failed to close data connections!", ex);
        }
        return false;
    }

    /**
     * The build() method will construct all the objects and their necessary dependencies that are
     * needed in the application scope..
     *
     * @return boolean  If true then build succeeded
     */
    private boolean build(String propertyFileName)
    {
        try
        {
            logger.info("------------------------------");
            logger.info("       INITIALIZING SAGE      ");
            logger.info("------------------------------");

            /** Setup application config */
            SageConfigurationListener configurationListener = new SageConfigurationListener();
            this.config = new Config(propertyFileName, configurationListener);
            this.baseDB = new DB(this.config, "db");
            this.tigerDB = new DB(this.config, "tiger.db");

            /** Setup service providers */
            addressServiceProvider = new AddressServiceProvider();
            addressServiceProvider.registerDefaultProvider("usps", USPS.class);
            addressServiceProvider.registerProvider("mapquest", MapQuest.class);
            addressServiceProvider.setProviderFallbackChain(Arrays.asList("mapquest"));

            geocodeServiceProvider = new GeocodeServiceProvider();
            geocodeServiceProvider.registerDefaultProvider("yahoo", Yahoo.class);
            geocodeServiceProvider.registerProvider("tiger", TigerGeocoder.class);
            geocodeServiceProvider.registerProvider("mapquest", MapQuest.class);
            geocodeServiceProvider.registerProvider("yahooboss", YahooBoss.class);
            geocodeServiceProvider.registerProvider("osm", OSM.class);
            geocodeServiceProvider.registerProvider("ruby", RubyGeocoder.class);
            geocodeServiceProvider.setProviderFallbackChain(Arrays.asList("mapquest", "tiger"));

            geocodeServiceProvider.registerProviderAsCacheable("yahoo");
            geocodeServiceProvider.registerProviderAsCacheable("yahooboss");
            geocodeServiceProvider.registerProviderAsCacheable("mapquest");

            revGeocodeServiceProvider = new RevGeocodeServiceProvider();
            revGeocodeServiceProvider.registerDefaultProvider("yahoo", Yahoo.class);
            revGeocodeServiceProvider.registerProvider("mapquest", MapQuest.class);
            revGeocodeServiceProvider.registerProvider("tiger", TigerGeocoder.class);
            revGeocodeServiceProvider.setProviderFallbackChain(Arrays.asList("mapquest", "tiger"));

            districtServiceProvider = new DistrictServiceProvider();
            districtServiceProvider.registerDefaultProvider("shapefile", DistrictShapefile.class);
            districtServiceProvider.registerProvider("streetfile", StreetFile.class);
            districtServiceProvider.registerProvider("geoserver", Geoserver.class);
            districtServiceProvider.setProviderFallbackChain(Arrays.asList("streetfile"));

            mapServiceProvider = new MapServiceProvider();
            mapServiceProvider.registerDefaultProvider("shapefile", DistrictShapefile.class);

            streetLookupServiceProvider = new StreetLookupServiceProvider();
            streetLookupServiceProvider.registerDefaultProvider("streetfile", StreetFile.class);

            cityZipServiceProvider = new CityZipServiceProvider();
            cityZipServiceProvider.registerDefaultProvider("cityZipDB", CityZipDB.class);

            logger.info("------------------------------");
            logger.info("            READY             ");
            logger.info("------------------------------");

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

    private boolean initCache()
    {
        logger.info("Loading Map and Senator Caches...");

        /** Initialize district map cache */
        DistrictShapefileDao dso = new DistrictShapefileDao();
        if (!dso.cacheDistrictMaps()) {
            logger.fatal("Failed to cache district maps!");
            return false;
        };

        /** Initialize senator cache */
        SenateDao sd = new SenateDao();
        Collection<Senator> senators = sd.getSenators();
        if (senators == null || senators.isEmpty()) {
            logger.fatal("Failed to cache senators!");
            return false;
        }

        return true;
    }

    public static Config getConfig() {
        return factoryInstance.config;
    }

    public static DataSource getDataSource() {
        return factoryInstance.baseDB.getDataSource();
    }

    public static DataSource getTigerDataSource() {
        return factoryInstance.tigerDB.getDataSource();
    }

    public static AddressServiceProvider getAddressServiceProvider() {
        return factoryInstance.addressServiceProvider;
    }

    public static DistrictServiceProvider getDistrictServiceProvider() {
        return factoryInstance.districtServiceProvider;
    }

    public static GeocodeServiceProvider getGeocodeServiceProvider()  {
        return factoryInstance.geocodeServiceProvider;
    }

    public static RevGeocodeServiceProvider getRevGeocodeServiceProvider() {
        return factoryInstance.revGeocodeServiceProvider;
    }

    public static MapServiceProvider getMapServiceProvider() {
        return factoryInstance.mapServiceProvider;
    }

    public static StreetLookupServiceProvider getStreetLookupServiceProvider() {
        return factoryInstance.streetLookupServiceProvider;
    }

    public static CityZipServiceProvider getCityZipServiceProvider() {
        return factoryInstance.cityZipServiceProvider;
    }
}