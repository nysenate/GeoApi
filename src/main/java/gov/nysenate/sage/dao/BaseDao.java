package gov.nysenate.sage.dao;

import gov.nysenate.sage.factory.ApplicationFactory;
import org.apache.commons.dbutils.AsyncQueryRunner;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.tomcat.jdbc.pool.DataSource;

import java.util.concurrent.ExecutorService;

public class BaseDao
{
    protected DataSource dataSource;

    public BaseDao()
    {
        this.dataSource = ApplicationFactory.getDataSource();
    }

    public QueryRunner getQueryRunner()
    {
        return new QueryRunner(this.dataSource);
    }

    public AsyncQueryRunner getAsyncQueryRunner(ExecutorService executorService)
    {
        return new AsyncQueryRunner(executorService, this.getQueryRunner());
    }


}
