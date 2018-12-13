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

import javax.sql.DataSource;
import java.beans.PropertyVetoException;

@EnableTransactionManagement
@Configuration
public class DatabaseConfig
{
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

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
    @Bean
    public DataSource geoApiPostgresDataSource() {
        final String jdbcUrlTemplate = "jdbc:%s://%s/%s";
        ComboPooledDataSource pool = new ComboPooledDataSource();
        try {
            pool.setDriverClass(dbDriver);
        }
        catch (PropertyVetoException ex) {
            logger.error("Error when setting the database driver " + dbDriver + "{}", ex.getMessage());
        }
        pool.setJdbcUrl(String.format(jdbcUrlTemplate, dbType, dbHost, dbName));
        logger.info("Connecting to Postgres: " + pool.getJdbcUrl());
        pool.setUser(dbUser);
        pool.setPassword(dbPass);
        pool.setMinPoolSize(1);
        pool.setMaxPoolSize(100);
        pool.setUnreturnedConnectionTimeout(30000);

        // Test each connection every 30 sec after first check-in
        pool.setTestConnectionOnCheckout(false);
        pool.setTestConnectionOnCheckin(true);
        pool.setIdleConnectionTestPeriod(30);
        return pool;
    }

    /**
     * Configures the sql data source using a connection pool.
     * @return DataSource
     */
    @Bean
    public DataSource tigerPostgresDataSource() {
        final String jdbcUrlTemplate = "jdbc:%s://%s/%s";
        ComboPooledDataSource pool = new ComboPooledDataSource();
        try {
            pool.setDriverClass(tigerDbDriver);
        }
        catch (PropertyVetoException ex) {
            logger.error("Error when setting the database driver " + tigerDbDriver + "{}", ex.getMessage());
        }
        pool.setJdbcUrl(String.format(jdbcUrlTemplate, tigerDbType, tigerDbHost, tigerDbName));
        logger.info("Connecting to Postgres: " + pool.getJdbcUrl());
        pool.setUser(tigerDbUser);
        pool.setPassword(tigerDbPass);
        pool.setMinPoolSize(1);
        pool.setMaxPoolSize(100);
        pool.setUnreturnedConnectionTimeout(30000);

        // Test each connection every 30 sec after first check-in
        pool.setTestConnectionOnCheckout(false);
        pool.setTestConnectionOnCheckin(true);
        pool.setIdleConnectionTestPeriod(30);
        return pool;
    }

    /**
     * Configures a Spring transaction manager for the postgres data source.
     * @return PlatformTransactionManager
     */
    @Bean
    public PlatformTransactionManager geoApiTransactionManager() {
        return new DataSourceTransactionManager(geoApiPostgresDataSource());
    }

    /**
     * Configures a Spring transaction manager for the postgres data source.
     * @return PlatformTransactionManager
     */
    @Bean
    public PlatformTransactionManager tigerTransactionManager() {
        return new DataSourceTransactionManager(tigerPostgresDataSource());
    }




}