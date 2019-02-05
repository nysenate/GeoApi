package gov.nysenate.sage.dao.logger;

import gov.nysenate.sage.BaseTests;
import gov.nysenate.sage.annotation.IntegrationTest;
import gov.nysenate.sage.config.DatabaseConfig;
import gov.nysenate.sage.dao.logger.point.SqlPointLogger;
import gov.nysenate.sage.model.geo.Point;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@Category(IntegrationTest.class)
public class PointLoggerTest extends BaseTests {

    @Autowired
    SqlPointLogger sqlPointLogger;

    @Test
    @Transactional(value = DatabaseConfig.geoApiTxManager)
    public void logPointTest() {
        int id = sqlPointLogger.logPoint(new Point(-43.498, 75.5));
        assertFalse((0 == id));
    }

    @Test
    @Transactional(value = DatabaseConfig.geoApiTxManager)
    public void getPointIdTest() {
        int insertedID = sqlPointLogger.logPoint(new Point(-43.498, 75.5));
        int retreivedID = sqlPointLogger.getPointId(new Point(-43.498, 75.5));
        assertEquals(insertedID, retreivedID);
    }

    @Test
    @Transactional(value = DatabaseConfig.geoApiTxManager)
    public void testInsertNullPoint() {
        int insertedID = sqlPointLogger.logPoint(null);
        assertEquals(-1, insertedID);
    }

}
