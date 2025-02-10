package gov.nysenate.sage.dao.provider;

import gov.nysenate.sage.BaseTests;
import gov.nysenate.sage.annotation.IntegrationTest;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.provider.geocache.GeoCache;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;

@Category(IntegrationTest.class)
public class GeocacheDaoIT extends BaseTests {
    @Autowired
    private GeoCache geoCache;

    @Test
    public void testCacheSave() {
        ArrayList<GeocodeResult> gcs = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            var a = Address.getAddress(i + "BOO" + " ST", "Test", "Test", "NY", "00001", "");
            Geocode gc = new Geocode(new Point("12", "12"), GeocodeQuality.HOUSE, "Test");
            gcs.add(new GeocodeResult(null, ResultStatus.SUCCESS, new GeocodedAddress(a, gc)));
        }
        geoCache.cache(gcs);
    }
}
