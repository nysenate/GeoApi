package gov.nysenate.sage.util;

import java.util.Observable;
import java.util.Observer;

import gov.nysenate.sage.util.Config;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

/**
 * Database class to configure and wrap a DataSource instance for performing queries.
 */
public class DB implements Observer
{
    private DataSource ds;
    private Config config;
    private String prefix;

    public DB(Config config, String dbPrefix)
    {
        this.config = config;
        this.config.notifyOnChange(this);
        this.buildDataSource(dbPrefix);
    }

    public DataSource getDataSource()
    {
        return this.ds;
    }

    public void update(Observable o, Object arg)
    {
        if (this.prefix != null) {
            buildDataSource(this.prefix);
        }
    };

    /**
     * Set up the data source.
     * The dbPrefix parameter specifies the prefix used in the properties file.
     * Thus for creating a data source for configuration db.name, db.host,..
     * set dbPrefix = "db". This is to allow for multiple databases.
     *
     * See the documentation for details:
     * http://people.apache.org/~fhanik/jdbc-pool/jdbc-pool.html
     */
    private void buildDataSource(String dbPrefix)
    {
        this.ds = new DataSource();
        this.prefix = dbPrefix;

        PoolProperties p = new PoolProperties();

        /** Basic connection parameters. */
        p.setUrl(String.format("jdbc:%s://%s/%s", config.getValue(dbPrefix + ".type"), config.getValue(dbPrefix + ".host"), config.getValue(dbPrefix + ".name")));
        p.setDriverClassName(config.getValue(dbPrefix + ".driver"));
        p.setUsername(config.getValue(dbPrefix + ".user"));
        p.setPassword(config.getValue(dbPrefix + ".pass"));

        /** How big should the connection pool be? How big can it get? */
        p.setInitialSize(10);
        p.setMaxActive(100);
        p.setMinIdle(10);
        p.setMaxIdle(100);

        /** Allow for 30 seconds between validating idle connections and cleaning abandoned connections. */
        p.setValidationInterval(30000);
        p.setTimeBetweenEvictionRunsMillis(30000);
        p.setMinEvictableIdleTimeMillis(30000);

        /** Configure the connection validation testing. */
        p.setTestOnBorrow(true);
        p.setTestOnReturn(false);
        p.setTestWhileIdle(false);
        p.setValidationQuery("SELECT 1");

        /**
         * Connections are considered abandoned after staying open for 60+ seconds
         * This should be set to longer than the longest expected query!
         */
        p.setLogAbandoned(true);
        p.setRemoveAbandoned(true);
        p.setRemoveAbandonedTimeout(60);

        /** How long should we wait for a connection before throwing an exception? */
        p.setMaxWait(10000);

        /** Not sure what JMX is... */
        p.setJmxEnabled(true);

        /** Interceptors implement hooks into the query process; like Tomcat filters.
         *  ConnectionState - Caches connection state information to avoid redundant queries.
         *  StatementFinalizer - Finalizes all related statements when a connection is closed.
         */
        p.setJdbcInterceptors("org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;" +
                "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");

        this.ds.setPoolProperties(p);
    }
}