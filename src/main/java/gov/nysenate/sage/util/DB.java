package gov.nysenate.sage.util;

import java.util.Observable;
import java.util.Observer;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

// ENUM Singleton - http://stackoverflow.com/a/71399
public enum DB implements Observer {
    INSTANCE;

    private DataSource db;

    private DB() {
        Config.notify(this);
    }

    public DataSource getDataSource() {
        if (db == null) {
            makeDB();
        }
        return db;
    }

    public void update(Observable o, Object arg) {
        makeDB();
    };

    private void makeDB() {
        // See the documentation for details:
        // http://people.apache.org/~fhanik/jdbc-pool/jdbc-pool.html
        PoolProperties p = new PoolProperties();

        // Basic connection parameters
        p.setUrl("jdbc:"+Config.read("db.type")+"://"+Config.read("db.host")+"/"+Config.read("db.name"));
        p.setDriverClassName(Config.read("db.driver"));
        p.setUsername(Config.read("db.user"));
        p.setPassword(Config.read("db.pass"));

        // How big should the connection pool be? How big can it get?
        p.setInitialSize(10);
        p.setMaxActive(100);
        p.setMinIdle(10);
        p.setMaxIdle(100);

        // Allow for 30 seconds between validating idle connections and cleaning abandoned connections
        p.setValidationInterval(30000);
        p.setTimeBetweenEvictionRunsMillis(30000);
        p.setMinEvictableIdleTimeMillis(30000);

        // Configure the connection validation testing.
        p.setTestOnBorrow(true);
        p.setTestOnReturn(false);
        p.setTestWhileIdle(false);
        p.setValidationQuery("SELECT 1");

        // Connections are considered abandoned after staying opne for 60+ seconds
        // This should be set to longer than the longest expected query!
        p.setLogAbandoned(true);
        p.setRemoveAbandoned(true);
        p.setRemoveAbandonedTimeout(60);

        // How long should we wait for a connection before throwing an exception
        p.setMaxWait(10000);

        // Not sure what JMX is...
        p.setJmxEnabled(true);

        // Interceptors implement hooks into the query process; like Tomcat filters.
        p.setJdbcInterceptors(
            // Caches connection state information to avoid redundant queries
            "org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"+
            // Finalizes all related statements when a connection is closed
            "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer"
        );

        db = new DataSource();
        db.setPoolProperties(p);
    }
}