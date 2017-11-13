package gov.nysenate.sage.dao.provider;

import gov.nysenate.sage.TestBase;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedStreetAddress;
import gov.nysenate.sage.model.address.StreetAddress;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.Line;
import gov.nysenate.sage.model.geo.Point;

import static gov.nysenate.sage.GeocodeTestBase.*;
import static org.junit.Assert.*;

import gov.nysenate.sage.provider.TigerGeocoder;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class TigerGeocoderDaoTest extends TestBase
{
    private TigerGeocoderDao tigerGeocoderDao;

    @Before
    public void setUp()
    {
        tigerGeocoderDao = new TigerGeocoderDao();
    }

    @Test
    public void TigerGeocoderSingleAddressGeocodeTest_ReturnsGeocodedStreetAddress()
    {
        GeocodedStreetAddress geocodedStreetAddress =
                tigerGeocoderDao.getGeocodedStreetAddress(new Address("214 8th Street", "Troy", "NY", "12180"));

        assertNotNull(geocodedStreetAddress);
        assertNotNull(geocodedStreetAddress.getGeocode());
        assertNotNull(geocodedStreetAddress.getStreetAddress());
        assertGeocodesAreSimilar(new Geocode(new Point(42.7352408, -73.6828174)), geocodedStreetAddress.getGeocode());
        assertEquals(214, geocodedStreetAddress.getStreetAddress().getBldgNum());
        assertEquals("8th st", geocodedStreetAddress.getStreetAddress().getStreet().toLowerCase());
        assertEquals("troy", geocodedStreetAddress.getStreetAddress().getLocation().toLowerCase());
        assertEquals("12180", geocodedStreetAddress.getStreetAddress().getZip5());
    }

    @Test
    public void TigerGeocoderInvalidAddressGeocodeTest_ReturnsNull()
    {
        GeocodedStreetAddress geocodedStreetAddress =
                tigerGeocoderDao.getGeocodedStreetAddress(new Address("", "", "", ""));

        assertNull(geocodedStreetAddress);
    }

    @Test
    public void TigerGeocoderSingleAddressParseTest_ReturnsStreetAddress()
    {
        StreetAddress streetAddress = tigerGeocoderDao.getStreetAddress(new Address("902 London Square Drive", "Clifton Park", "NY", "12065"));
        assertNotNull(streetAddress);
        assertEquals(902, streetAddress.getBldgNum());
        assertEquals("london square dr", streetAddress.getStreet().toLowerCase());
        assertEquals("dr", streetAddress.getStreetType().toLowerCase());
        assertEquals("clifton park", streetAddress.getLocation().toLowerCase());
        assertEquals("NY", streetAddress.getState().toUpperCase());
        assertEquals("12065", streetAddress.getZip5());
    }

    @Test
    public void TigerGeocoderSingleAddressReverseGeocode_ReturnsStreetAddress()
    {
        StreetAddress streetAddress = tigerGeocoderDao.getStreetAddress(new Point(42.7352408, -73.6828174));
        assertNotNull(streetAddress);
        assertEquals("troy", streetAddress.getLocation().toLowerCase());
        assertEquals("12180", streetAddress.getZip5());
    }

    @Test
    public void miscTest()
    {
        assertNotNull(tigerGeocoderDao.getGeocodedStreetAddress(new Address("100 Nyroy Dr Troy NY 12180")));
    }

    @Test
    public void getStreetsInZipTest() {
        assertNotNull(tigerGeocoderDao.getStreetsInZip("12180"));
    }

    @Test
    public void getStreetLineGeometryTest()
    {
        List<Line> lines = tigerGeocoderDao.getStreetLineGeometry("State St", Arrays.asList("12203", "12210"));
        assertNotNull(lines);
    }
}