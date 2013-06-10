package gov.nysenate.sage.dao;

import gov.nysenate.sage.TestBase;
import gov.nysenate.sage.dao.provider.TigerGeocoderDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedStreetAddress;
import gov.nysenate.sage.model.address.StreetAddress;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.Point;

import static gov.nysenate.sage.GeocodeTestBase.*;

import gov.nysenate.sage.util.FormatUtil;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class TigerGeocoderDaoTest extends TestBase
{
    private static Logger logger = Logger.getLogger(TigerGeocoderDaoTest.class);
    private TigerGeocoderDao tigerGeocoderDao;

    @Before
    public void setUp()
    {
        tigerGeocoderDao = new TigerGeocoderDao();
    }

    @Test
    public void testQueryTimeOut()
    {
        /** This incorrect address causes the geocoder to search exhaustively if a
         * time out is not set. */
        Address incorrectAddress = new Address("9264 224 st", "Queens", "NY", "11432");
        GeocodedStreetAddress timedOutGsa = tigerGeocoderDao.getGeocodedStreetAddress(incorrectAddress);
        assertNull(timedOutGsa);
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
        assertEquals("8th", geocodedStreetAddress.getStreetAddress().getStreet());
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
        StreetAddress streetAddress = tigerGeocoderDao.getStreetAddress(new Address("214 8th Street", "Troy", "NY", "12180"));
        assertNotNull(streetAddress);
        assertEquals(214, streetAddress.getBldgNum());
        assertEquals("8th", streetAddress.getStreet());
        assertEquals("st", streetAddress.getStreetType().toLowerCase());
        assertEquals("troy", streetAddress.getLocation().toLowerCase());
        assertEquals("NY", streetAddress.getState().toUpperCase());
        assertEquals("12180", streetAddress.getZip5());
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
        FormatUtil.printObject(tigerGeocoderDao.getGeocodedStreetAddress(new Address("100 Nyroy Dr Troy NY 12180")));
    }
}