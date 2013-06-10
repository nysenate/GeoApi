package gov.nysenate.sage.util;

import gov.nysenate.sage.TestBase;
import gov.nysenate.sage.factory.ApplicationFactory;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ArrayHandler;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.junit.Test;
import static org.junit.Assert.*;

public class DBTest extends TestBase
{
    /**
     * Quick test to see if the database connection is working.
     * @throws Exception
     */
    @Test
    public void DBConnectionTest() throws Exception
    {
        DataSource ds = ApplicationFactory.getDataSource();
        QueryRunner qr = new QueryRunner(ds);
        ArrayHandler handler = new ArrayHandler();
        Object[] result = qr.query("SELECT 1", handler );

        assertEquals(String.valueOf(1), String.valueOf(result[0]));
    }
}