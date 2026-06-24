package gov.nysenate.sage.dao.provider;

import gov.nysenate.sage.BaseTests;
import gov.nysenate.sage.annotation.IntegrationTest;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.Accuracy;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.provider.geocache.GeoCache;
import gov.nysenate.sage.provider.geocode.Geocoder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

@Category(IntegrationTest.class)
public class GeocacheDaoIT extends BaseTests {
    @Autowired
    private GeoCache geoCache;

    @Test
    public void testCacheSave() {
        for (int i = 0; i < 100; i++) {
            var a = new Address(i + "BOO" + " ST", "Test", "Test", "NY", "00001", "");
            var gc = new Geocode(new Point("12", "12"), Accuracy.HOUSE, Geocoder.GEOCACHE, false);
            var currResult = new GeocodeResult(null, ResultStatus.SUCCESS, new GeocodedAddress(a, gc));
            geoCache.cache(currResult.getGeocodedAddress());
        }
    }
}
