package gov.nysenate.sage.util;

import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.Point;
import org.junit.Test;
import static org.junit.Assert.*;

public class GeocodeUtilTest
{
    @Test
    public void getDistanceInMetersUsingGeocodesTest()
    {
        Geocode g1 = new Geocode(new Point(43.1750357028, -77.4981899685));
        Geocode g2 = new Geocode(new Point(43.17511, -77.497963));
        assertTrue(GeocodeUtil.getDistanceInFeet(g1, g2) < 70.0);
        assertTrue(GeocodeUtil.getDistanceInFeet(g1, g2) > 50.0);
    }

    @Test
    public void getDistanceInMetersUsingBadGeocodesTest()
    {
        Geocode g1 = new Geocode(new Point(43.1750357028, -77.4981899685));
        Geocode g2 = new Geocode();
        assertEquals(new Double(-1.0), GeocodeUtil.getDistanceInFeet(g1, g2));
    }
}
