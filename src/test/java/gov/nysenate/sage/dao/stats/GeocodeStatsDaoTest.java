package gov.nysenate.sage.dao.stats;

import gov.nysenate.sage.TestBase;
import gov.nysenate.sage.model.stats.GeocodeStats;
import gov.nysenate.sage.util.FormatUtil;
import org.junit.Before;
import org.junit.Test;

public class GeocodeStatsDaoTest extends TestBase
{
    private GeocodeStatsDao geocodeStatsDao;

    @Before
    public void setUp()
    {
        this.geocodeStatsDao = new GeocodeStatsDao();
    }

    @Test
    public void getLifetimeGeocodeStatsTest()
    {
        GeocodeStats gs = this.geocodeStatsDao.getLifetimeGeocodeStats();
        FormatUtil.printObject(gs);
    }

    @Test
    public void getCurrentGeocodeStatsTest()
    {
        GeocodeStats gs = this.geocodeStatsDao.getCurrentGeocodeStats();
        FormatUtil.printObject(gs);
    }
}
