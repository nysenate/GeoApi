package gov.nysenate.sage.provider;

import gov.nysenate.sage.TestBase;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.util.FormatUtil;
import org.junit.Test;

public class GeoCacheTest extends TestBase
{
    GeoCache geoCache = new GeoCache();

    @Test
    public void geocodeTest()
    {
        //geoCache.saveToCache();
        FormatUtil.printObject(GeoCache.isProviderCacheable(TigerGeocoder.class));
    }

}
