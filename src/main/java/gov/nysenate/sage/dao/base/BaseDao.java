package gov.nysenate.sage.dao.base;

import gov.nysenate.sage.factory.ApplicationFactory;
import org.apache.commons.dbutils.AsyncQueryRunner;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.log4j.Logger;
import org.apache.tomcat.jdbc.pool.DataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;

public class BaseDao
{
    private static Logger logger = Logger.getLogger(BaseDao.class);
    protected DataSource dataSource;
    protected DataSource tigerDataSource;

    public BaseDao()
    {
        this.dataSource = ApplicationFactory.getDataSource();
        this.tigerDataSource = ApplicationFactory.getTigerDataSource();
    }

    public QueryRunner getQueryRunner()
    {
        return new QueryRunner(this.dataSource);
    }

    public AsyncQueryRunner getAsyncQueryRunner(ExecutorService executorService)
    {
        return new AsyncQueryRunner(executorService, this.getQueryRunner());
    }

    public QueryRunner getTigerQueryRunner()
    {
        return new QueryRunner(this.tigerDataSource);
    }

    public AsyncQueryRunner getAsyncTigerQueryRunner(ExecutorService executorService)
    {
        return new AsyncQueryRunner(executorService, this.getTigerQueryRunner());
    }

    public Connection getConnection()
    {
        try {
            return this.dataSource.getConnection();
        }
        catch (SQLException ex) {
            logger.fatal(ex.getMessage());
        }
        return null;
    }

    public Connection getTigerConnection()
    {
        try {
            return this.tigerDataSource.getConnection();
        }
        catch (SQLException ex) {
            logger.fatal(ex.getMessage());
        }
        return null;
    }

    public void closeConnection(Connection connection)
    {
        try {
            if (connection != null && !connection.isClosed()){
                connection.close();
            }
        }
        catch (SQLException ex){
            logger.fatal("Failed to close connection!", ex);
        }
    }

    /**
     * Some geocoder queries don't know when to call it quits. Call this method before a query
     * to set the given timeout. If the query does time out a SQLException will be thrown.
     * @param timeOutInMs
     * @return
     */
    protected void setTimeOut(Connection conn, QueryRunner run, int timeOutInMs) throws SQLException
    {
        String setTimeout = "SET statement_timeout TO " + timeOutInMs + ";";
        run.update(conn, setTimeout);
    }

    /**
     * It's a good idea to reset the timeout after the query is done.
     * @return
     */
    protected void resetTimeOut(Connection conn, QueryRunner run) throws SQLException
    {
        String setTimeout = "RESET statement_timeout;";
        run.update(conn, setTimeout);
    }
}
