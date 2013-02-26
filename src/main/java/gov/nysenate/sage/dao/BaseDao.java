package gov.nysenate.sage.dao;

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

}
