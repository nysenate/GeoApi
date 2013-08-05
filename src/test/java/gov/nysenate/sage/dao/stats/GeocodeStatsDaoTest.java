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
    public void getGeocodeStatsTest()
    {

    }
}
