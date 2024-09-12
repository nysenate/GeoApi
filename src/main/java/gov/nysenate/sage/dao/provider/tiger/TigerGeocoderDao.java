package gov.nysenate.sage.dao.provider.tiger;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedStreetAddress;
import gov.nysenate.sage.model.address.StreetAddress;
import gov.nysenate.sage.model.geo.Line;
import gov.nysenate.sage.model.geo.Point;

import java.util.List;

public interface TigerGeocoderDao {
    /**
     * Performs geocoding and returns a GeocodedStreetAddress. A timeout is also enabled because some queries
     * can just go on indefinitely.
     */
    GeocodedStreetAddress getGeocodedStreetAddress(Address address);

    /**
     * Reverse geocodes a point and returns a StreetAddress that is close to that point. The
     * reverse geocoder actually returns an intersection but to keep the model simple the first
     * street address is returned.
     * @param point Point to reverse geocode
     * @return      StreetAddress or null if no matches
     */
    StreetAddress getStreetAddress(Point point);

    /**
     * Retrieves a list of street names that are contained within the supplied zipcode
     * @return List<String>
     */
    List<String> getStreetsInZip(String zip5);

    /**
     * Retrieves JSON geometry for a street that is matched in the set of zip5s.
     * @return GeoJSON string or null if no match.
     */
    String getStreetLineGeometryAsJson(String streetName, List<Integer> zip5List);

    /**
     * Retrieves a collection of line objects by processing the result of getStreetLineGeometryAsJson().
     */
    List<Line> getStreetLineGeometry(String streetName, List<Integer> zip5List);
}
