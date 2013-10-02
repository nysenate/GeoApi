package gov.nysenate.sage.provider;

import gov.nysenate.sage.TestBase;
import gov.nysenate.sage.dao.provider.GeoCacheDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedStreetAddress;
import gov.nysenate.sage.model.address.StreetAddress;
import gov.nysenate.sage.util.FormatUtil;
import org.junit.Test;

public class GeoCacheTest extends TestBase
{
    GeoCache geoCache = new GeoCache();
    GeoCacheDao geoCacheDao = new GeoCacheDao();

    @Test
    public void geocodeTest()
    {
        StreetAddress sa = new StreetAddress();
        sa.setBldgNum(101);
        sa.setStreetName("NYROY");
        sa.setStreetType("DR");
        sa.setState("NY");
        sa.setZip5("12180");
        FormatUtil.printObject(geoCacheDao.getCacheHit(sa));
    }

}
