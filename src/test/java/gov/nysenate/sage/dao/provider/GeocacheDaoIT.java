package gov.nysenate.sage.dao.provider;

import gov.nysenate.sage.BaseTests;
import gov.nysenate.sage.annotation.IntegrationTest;
import gov.nysenate.sage.config.DatabaseConfig;
import gov.nysenate.sage.dao.provider.geocache.SqlGeoCacheDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.util.TimeUtil;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;

@Category(IntegrationTest.class)
public class GeocacheDaoIT extends BaseTests {

    @Autowired
    SqlGeoCacheDao sqlGeoCacheDao;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    @Transactional(value = DatabaseConfig.geocoderTxManager)
    public void testCacheSave()
    {
        ArrayList<GeocodedAddress> gcs = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Address a = new Address(i+"BOO" + " ST", "Test", "NY", "00001");
            Geocode gc = new Geocode(new Point(12, 12), GeocodeQuality.HOUSE, "Test");
            gcs.add(new GeocodedAddress(a, gc));
        }

        Timestamp start = TimeUtil.currentTimestamp();
        sqlGeoCacheDao.cacheGeocodedAddresses(gcs);
        sqlGeoCacheDao.flushCacheBuffer();
        logger.info("Elapsed time: " + TimeUtil.getElapsedMs(start) + " ms.");
    }
}
