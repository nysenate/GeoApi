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

    public DB(Config config)
    {
        this.config = config;
        this.config.notifyOnChange(this);
        this.buildDataSource();
    }

    public DataSource getDataSource()
    {
        return this.ds;
    }

    public void update(Observable o, Object arg)
    {
        buildDataSource();
    };

    /**
     * Set up the data source. See the documentation for details:
     * http://people.apache.org/~fhanik/jdbc-pool/jdbc-pool.html
     */
    private void buildDataSource()
    {
        this.ds = new DataSource();
        PoolProperties p = new PoolProperties();

        /** Basic connection parameters. */
        p.setUrl(String.format("jdbc:%s://%s/%s", config.getValue("db.type"), config.getValue("db.host"), config.getValue("db.name")));
        p.setDriverClassName(config.getValue("db.driver"));
        p.setUsername(config.getValue("db.user"));
        p.setPassword(config.getValue("db.pass"));

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