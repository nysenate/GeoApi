package gov.nysenate.sage.provider;

import gov.nysenate.sage.TestBase;
import gov.nysenate.sage.dao.provider.GeoCacheDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedStreetAddress;
import gov.nysenate.sage.model.address.StreetAddress;
import gov.nysenate.sage.util.FormatUtil;
import gov.nysenate.sage.util.StreetAddressParser;
import org.junit.Test;

public class GeoCacheTest extends TestBase
{
    GeoCache geoCache = new GeoCache();
    GeoCacheDao geoCacheDao = new GeoCacheDao();

    @Test
    public void geocodeTest()
    {
        StreetAddress sa = StreetAddressParser.parseAddress(new Address("", "Jamaica", "NY", ""));
        FormatUtil.printObject(sa.toStringParsed());
        FormatUtil.printObject(geoCacheDao.getCacheHit(sa));
    }

}
