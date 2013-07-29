package gov.nysenate.sage.dao.logger;

import gov.nysenate.sage.TestBase;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.util.FormatUtil;
import org.junit.Before;
import org.junit.Test;

public class PointLoggerTest extends TestBase
{
    private PointLogger pointLogger;

    @Before
    public void setUp() {
        pointLogger = new PointLogger();
    }

    @Test
    public void getPointIdTest() {
        FormatUtil.printObject(pointLogger.getPointId(new Point(-43.4, 75.5)));
    }

    @Test
    public void logPointTest() {
        FormatUtil.printObject(pointLogger.logPoint(new Point(-43.498, 75.5)));
    }
}
