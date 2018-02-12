package gov.nysenate.sage.factory;

import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.dao.logger.AddressLogger;
import gov.nysenate.sage.dao.model.SenateDao;
import gov.nysenate.sage.dao.provider.DistrictShapefileDao;
import gov.nysenate.sage.listener.SageConfigurationListener;
import gov.nysenate.sage.provider.*;
import gov.nysenate.sage.service.address.AddressService;
import gov.nysenate.sage.service.address.AddressServiceProvider;
import gov.nysenate.sage.service.address.CityZipServiceProvider;
import gov.nysenate.sage.service.address.ParallelAddressService;
import gov.nysenate.sage.service.district.DistrictServiceProvider;
import gov.nysenate.sage.service.district.ParallelDistrictService;
import gov.nysenate.sage.service.geo.*;
import gov.nysenate.sage.service.map.MapServiceProvider;
import gov.nysenate.sage.service.street.StreetLookupServiceProvider;
import gov.nysenate.sage.util.Config;
import gov.nysenate.sage.util.DB;
import gov.nysenate.services.model.Senator;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

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
@Component
public class ApplicationFactory
{

    /** Dependency instances */
    private Config config;
    private DB baseDB;
    private DB tigerDB;

    private Environment env;

    /** Default values */
    private static String defaultPropertyFileName = "app.properties";

    @Autowired
    public ApplicationFactory(Environment env) {
        this.env = env;

    }

    public Config getConfig() {
        return config;
    }

    public DataSource getDataSource() {
        return baseDB.getDataSource();
    }

    public DataSource getTigerDataSource() {
        return tigerDB.getDataSource();
    }
}
