package gov.nysenate.sage.dao.provider;

import gov.nysenate.sage.GeocodeTestBase;
import gov.nysenate.sage.TestBase;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.util.FormatUtil;
import org.junit.Test;

import java.util.ArrayList;

public class GeoCacheDaoTest extends TestBase
{
    GeoCacheDao geoCacheDao = new GeoCacheDao();

    @Test
    public void testGetCacheHit()
    {
        //FormatUtil.printObject(geoCacheDao.getCacheHit(new Address("2012 East River Road, Olean, NY 14760")));
    }

    @Test
    public void testCacheSave()
    {
        ArrayList<Address> addressses = GeocodeTestBase.addresses;
        ArrayList<Geocode> geocodes = GeocodeTestBase.expectedGeocode;
        geoCacheDao.cacheGeocodedAddress(new GeocodedAddress(addressses.get(0), geocodes.get(0)));
        geoCacheDao.cacheGeocodedAddress(new GeocodedAddress(addressses.get(1), geocodes.get(1)));
        geoCacheDao.cacheGeocodedAddress(new GeocodedAddress(addressses.get(2), geocodes.get(2)));
        geoCacheDao.cacheGeocodedAddress(new GeocodedAddress(addressses.get(3), geocodes.get(3)));
        geoCacheDao.flushCacheBuffer();
    }
}
