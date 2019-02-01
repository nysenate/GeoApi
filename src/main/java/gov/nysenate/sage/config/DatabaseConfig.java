package gov.nysenate.sage.config;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.beans.PropertyVetoException;

@EnableTransactionManagement
@Configuration
public class DatabaseConfig
{
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    public static final String geoApiTxManager = "geoApiTxManager";
    public static final String geocoderTxManager = "geocoderTxManager";

    /** PostgreSQL Database Configuration */
    @Value("${db.driver}") private String dbDriver;
    @Value("${db.type}")  private String dbType;
    @Value("${db.host}")  private String dbHost;
    @Value("${db.name}")  private String dbName;
    @Value("${db.user}")  private String dbUser;
    @Value("${db.pass}")  private String dbPass;

    /** PostgreSQL Database Configuration */
    @Value("${tiger.db.driver}") private String tigerDbDriver;
    @Value("${tiger.db.type}")  private String tigerDbType;
    @Value("${tiger.db.host}")  private String tigerDbHost;
    @Value("${tiger.db.name}")  private String tigerDbName;
    @Value("${tiger.db.user}")  private String tigerDbUser;
    @Value("${tiger.db.pass}")  private String tigerDbPass;

    @Bean
    public JdbcTemplate geoApiJdbcTemplate() {
        return new JdbcTemplate(geoApiPostgresDataSource());
    }

    @Bean
    public NamedParameterJdbcTemplate geoApiNamedJdbcTemplate() {
        return new NamedParameterJdbcTemplate(geoApiPostgresDataSource());
    }

    @Bean
    public JdbcTemplate tigerJdbcTemplate() {
        return new JdbcTemplate(tigerPostgresDataSource());
    }

    @Bean
    public NamedParameterJdbcTemplate tigerNamedJdbcTemplate() {
        return new NamedParameterJdbcTemplate(tigerPostgresDataSource());
    }

    /**
     * Configures the sql data source using a connection pool.
     * @return DataSource
     */
    @Bean(destroyMethod = "close")
    public ComboPooledDataSource geoApiPostgresDataSource() {
        ComboPooledDataSource pool = getComboPooledDataSource(dbType,dbHost,dbName,dbDriver,dbUser, dbPass);
        logger.info("Connecting to Postgres: " + pool.getJdbcUrl());
        return pool;
    }

    /**
     * Configures the sql data source using a connection pool.
     * @return DataSource
     */
    @Bean(destroyMethod = "close")
    public ComboPooledDataSource tigerPostgresDataSource() {
        ComboPooledDataSource pool = getComboPooledDataSource(tigerDbType, tigerDbHost, tigerDbName, tigerDbDriver,
                tigerDbUser, tigerDbPass);
        logger.info("Connecting to Postgres: " + pool.getJdbcUrl());
        return pool;
    }

    /**
     * Creates a basic pooled DataSource.
     *
     * @param type Database type
     * @param host Database host address
     * @param name Database name
     * @param driver Database driver string
     * @param user Database user
     * @param pass Database password
     * @return PoolProperties
     */
    private ComboPooledDataSource getComboPooledDataSource(String type, String host, String name, String driver,
                                                           String user, String pass) {
        final String jdbcUrlTemplate = "jdbc:%s://%s/%s";
        ComboPooledDataSource pool = new ComboPooledDataSource();
        try {
            pool.setDriverClass(driver);
        }
        catch (PropertyVetoException ex) {
            logger.error("Error when setting the database driver " + driver + "{}", ex.getMessage());
        }
        final String jdbcUrl = String.format(jdbcUrlTemplate, type, host, name);

        pool.setJdbcUrl(jdbcUrl);
        pool.setUser(user);
        pool.setPassword(pass);

        pool.setMinPoolSize(1);
        pool.setMaxPoolSize(100);
        pool.setUnreturnedConnectionTimeout(30000);

        return pool;
    }

    /**
     * Configures a Spring transaction manager for the postgres data source.
     * @return PlatformTransactionManager
     */
    @Bean(name = "geoApiTxManager")
    public PlatformTransactionManager geoApiTransactionManager() {
        return new DataSourceTransactionManager(geoApiPostgresDataSource());
    }

    /**
     * Configures a Spring transaction manager for the postgres data source.
     * @return PlatformTransactionManager
     */
    @Bean(name = "geocoderTxManager")
    public PlatformTransactionManager tigerTransactionManager() {
        return new DataSourceTransactionManager(tigerPostgresDataSource());
    }




}