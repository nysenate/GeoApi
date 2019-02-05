package gov.nysenate.sage.dao.provider;

import gov.nysenate.sage.BaseTests;
import gov.nysenate.sage.annotation.IntegrationTest;
import gov.nysenate.sage.config.DatabaseConfig;
import gov.nysenate.sage.dao.provider.tiger.SqlTigerGeocoderDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedStreetAddress;
import gov.nysenate.sage.model.geo.Line;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@Category(IntegrationTest.class)
public class TigerGeocoderDaoTest extends BaseTests {

    @Autowired
    SqlTigerGeocoderDao sqlTigerGeocoderDao;

    @Test
    @Transactional(value = DatabaseConfig.geocoderTxManager)
    public void TigerGeocoderSingleAddressGeocodeTest() {
        GeocodedStreetAddress geocodedStreetAddress =
                sqlTigerGeocoderDao.getGeocodedStreetAddress(new Address("214 8th Street", "Troy", "NY", "12180"));

        assertNotNull(geocodedStreetAddress);
        assertNotNull(geocodedStreetAddress.getGeocode());
        assertNotNull(geocodedStreetAddress.getStreetAddress());
    }

    @Test
    @Transactional(value = DatabaseConfig.geocoderTxManager)
    public void TigerGeocoderInvalidAddressGeocodeTest() {
        GeocodedStreetAddress geocodedStreetAddress =
                sqlTigerGeocoderDao.getGeocodedStreetAddress(new Address("", "", "", ""));

        assertNull(geocodedStreetAddress);
    }

    @Test
    @Transactional(value = DatabaseConfig.geocoderTxManager)
    public void miscTest()
    {
        assertNotNull(sqlTigerGeocoderDao.getGeocodedStreetAddress(new Address("100 Nyroy Dr Troy NY 12180")));
    }

    @Test
    @Transactional(value = DatabaseConfig.geocoderTxManager)
    public void getStreetsInZipTest() {
        assertNotNull(sqlTigerGeocoderDao.getStreetsInZip("12180"));
    }

    @Test
    @Transactional(value = DatabaseConfig.geocoderTxManager)
    public void getStreetLineGeometryTest()
    {
        List<Line> lines = sqlTigerGeocoderDao.getStreetLineGeometry("State St", Arrays.asList("12203", "12210"));
        assertNotNull(lines);
    }
}
