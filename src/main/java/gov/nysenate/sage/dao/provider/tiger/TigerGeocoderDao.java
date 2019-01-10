package gov.nysenate.sage.dao.provider.tiger;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedStreetAddress;
import gov.nysenate.sage.model.address.StreetAddress;
import gov.nysenate.sage.model.geo.Line;
import gov.nysenate.sage.model.geo.Point;

import java.sql.Connection;
import java.util.List;

public interface TigerGeocoderDao {

    /**
     * Performs geocoding and returns a GeocodedStreetAddress. A timeout is also enabled because some queries
     * can just go on indefinitely.
     * @param address
     * @return
     */
    public GeocodedStreetAddress getGeocodedStreetAddress(Address address);

    /**
     * This method may be used to parse an Address into it's street address components using
     * Tiger Geocoder's built in address parser.
     * @param address   Address to parse
     * @return          Street Address containing the parsed components
     */
    public StreetAddress getStreetAddress(Address address);

    /**
     * Reverse geocodes a point and returns a StreetAddress that is close to that point. The
     * reverse geocoder actually returns an intersection but to keep the model simple the first
     * street address is returned.
     * @param point Point to reverse geocode
     * @return      StreetAddress or null if no matches
     */
    public StreetAddress getStreetAddress(Point point);

    /**
     * Retrieves a list of street names that are contained within the supplied zipcode
     * @param zip5
     * @return List<String>
     */
    public List<String> getStreetsInZip(String zip5);

    /**
     * Retrieves JSON geometry for a street that is matched in the set of zip5s.
     * @param streetName
     * @param zip5List
     * @return GeoJSON string or null if no match.
     */
    public String getStreetLineGeometryAsJson(String streetName, List<String> zip5List);

    /**
     * Retrieves a collection of line objects by processing the result of getStreetLineGeometryAsJson().
     * @param streetName
     * @param zip5List
     * @return
     */
    public List<Line> getStreetLineGeometry(String streetName, List<String> zip5List);


}
