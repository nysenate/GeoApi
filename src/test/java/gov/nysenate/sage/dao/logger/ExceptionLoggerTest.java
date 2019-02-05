package gov.nysenate.sage.dao.logger;

import gov.nysenate.sage.BaseTests;
import gov.nysenate.sage.annotation.IntegrationTest;
import gov.nysenate.sage.config.DatabaseConfig;
import gov.nysenate.sage.dao.logger.exception.SqlExceptionLogger;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Date;

@Category(IntegrationTest.class)
public class ExceptionLoggerTest extends BaseTests {

    @Autowired
    SqlExceptionLogger sqlExceptionLogger;

    @Test
    @Transactional(value = DatabaseConfig.geoApiTxManager)
    public void testInsertException() {
        sqlExceptionLogger.logException(new RuntimeException(), new Timestamp(new Date().getTime()), 999999999);
    }
}
