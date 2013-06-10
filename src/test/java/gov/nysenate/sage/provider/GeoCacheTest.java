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
        FormatUtil.printObject(geoCache.geocode(new Address("214 8th St, Troy NY 12180")));
    }

}
