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
public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    public static final String geoApiTxManager = "txManager";

    /** PostgreSQL Database Configuration */
    @Value("${db.driver:org.postgresql.Driver}") private String dbDriver;
    @Value("${db.type}")  private String dbType;
    @Value("${db.host}")  private String dbHost;
    @Value("${db.name}")  private String dbName;
    @Value("${db.user}")  private String dbUser;
    @Value("${db.pass}")  private String dbPass;

    @Bean
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(postgresDataSource());
    }

    @Bean
    public NamedParameterJdbcTemplate namedJdbcTemplate() {
        return new NamedParameterJdbcTemplate(postgresDataSource());
    }

    /**
     * Configures the sql data source using a connection pool.
     * @return DataSource
     */
    @Bean(destroyMethod = "close")
    public ComboPooledDataSource postgresDataSource() {
        ComboPooledDataSource pool = getComboPooledDataSource(dbType,dbHost,dbName,dbDriver,dbUser, dbPass);
        logger.info("Connecting to Postgres: {}", pool.getJdbcUrl());
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
            logger.error("Error when setting the database driver {}{}", driver, ex.getMessage());
        }
        final String jdbcUrl = String.format(jdbcUrlTemplate, type, host, name);

        pool.setJdbcUrl(jdbcUrl);
        pool.setUser(user);
        pool.setPassword(pass);

        pool.setMinPoolSize(1);
        pool.setMaxPoolSize(100);
        pool.setUnreturnedConnectionTimeout(30000);

        // Test each connection every 60 sec after first check-in
        pool.setTestConnectionOnCheckout(false);
        pool.setTestConnectionOnCheckin(true);
        pool.setIdleConnectionTestPeriod(60);
        // Fast query to execute when testing connections
        pool.setPreferredTestQuery("SELECT 1");

        return pool;
    }

    /**
     * Configures a Spring transaction manager for the postgres data source.
     * @return PlatformTransactionManager
     */
    @Bean(name = geoApiTxManager)
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(postgresDataSource());
    }
}
