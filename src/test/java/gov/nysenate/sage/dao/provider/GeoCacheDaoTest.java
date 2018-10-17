package gov.nysenate.sage.dao.provider;

import java.sql.Timestamp;
import java.util.ArrayList;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.nysenate.sage.GeocodeTestBase;
import gov.nysenate.sage.TestBase;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.util.TimeUtil;


public class GeoCacheDaoTest extends TestBase
{
    GeoCacheDao geoCacheDao = new GeoCacheDao();
    Logger logger = LoggerFactory.getLogger(GeoCacheDaoTest.class);

    @Test
    public void testCacheSave()
    {
        ArrayList<GeocodedAddress> gcs = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Address a = new Address("12 MOO" + " ST", "Test", "NY", "00001");
            Geocode gc = new Geocode(new Point(12, 12), GeocodeQuality.HOUSE, "Test");
            gcs.add(new GeocodedAddress(a, gc));
        }
        Address a = new Address("13 MOO" + " ST", "Test", "NY", "00001");
        Geocode gc = new Geocode(new Point(12, 12), GeocodeQuality.HOUSE, "Test");
        gcs.add(new GeocodedAddress(a, gc));

        Timestamp start = TimeUtil.currentTimestamp();
        geoCacheDao.cacheGeocodedAddresses(gcs);
        geoCacheDao.flushCacheBuffer();
        logger.info("Elapsed time: " + TimeUtil.getElapsedMs(start) + " ms.");
    }
}
