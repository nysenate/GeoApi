package gov.nysenate.sage.dao.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedStreetAddress;
import gov.nysenate.sage.model.address.StreetAddress;
import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.Line;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.geo.Polygon;
import gov.nysenate.sage.util.FormatUtil;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.BeanProcessor;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static org.apache.commons.dbutils.DbUtils.close;

/**
 * Provides a query interface to the TigerGeocoder database. For documentation on the geocoder
 * and it's available commands, refer to the following documentation (link subject to change):
 *
 * http://postgis.net/docs/Extras.html
 */
public class TigerGeocoderDao extends BaseDao
{
    private static Logger logger = Logger.getLogger(TigerGeocoderDao.class);
    private QueryRunner run = getTigerQueryRunner();
    private int GEOCODER_TIMEOUT = 1000; //ms

    private enum GeometryTypes {
        LINESTRING("LineString"), MULTILINESTRING("MultiLineString");
        public String type;
        GeometryTypes(String type) {
            this.type = type;
        }
    }

    /**
     * Performs geocoding and returns a GeocodedStreetAddress. A timeout is also enabled because some queries
     * can just go on indefinitely.
     * @param conn
     * @param address
     * @return
     */
    public GeocodedStreetAddress getGeocodedStreetAddress(Connection conn, Address address)
    {
        GeocodedStreetAddress geoStreetAddress = null;
        String sql = "SELECT g.rating, ST_Y(geomout) As lat, ST_X(geomout) As lon, (addy).* \n" +
                     "FROM geocode(?, 1) AS g;";
        try {
            setTimeOut(conn, run, GEOCODER_TIMEOUT);
            geoStreetAddress = run.query(conn, sql, new GeocodedStreetAddressHandler(), address.toString());
        }
        catch (SQLException ex){
            logger.warn(ex.getMessage());
        }
        finally {
            closeConnection(conn);
        }

        return geoStreetAddress;
    }

    public GeocodedStreetAddress getGeocodedStreetAddress(Address address)
    {
        return getGeocodedStreetAddress(this.getTigerConnection(), address);
    }

    /**
     * This method may be used to parse an Address into it's street address components using
     * Tiger Geocoder's built in address parser.
     * @param address   Address to parse
     * @return          Street Address containing the parsed components
     */
    public StreetAddress getStreetAddress(Address address)
    {
        String sql = "SELECT * FROM normalize_address(?)";
        try {
            StreetAddress streetAddress = run.query(sql, new StreetAddressHandler(), address.toNormalizedString());
            return streetAddress;
        }
        catch (SQLException ex){
            logger.error(ex.getMessage());
        }
        return null;
    }

    /**
     * Reverse geocodes a point and returns a StreetAddress that is close to that point. The
     * reverse geocoder actually returns an intersection but to keep the model simple the first
     * street address is returned.
     * @param point Point to reverse geocode
     * @return      StreetAddress or null if no matches
     */
    public StreetAddress getStreetAddress(Point point)
    {
        String sql = "SELECT (addy[1]).* " +
                     "FROM reverse_geocode(ST_GeomFromText('POINT(" + point.getLon() + " " + point.getLat() + ")',4269),true) As r;";
        try {
            if (point != null){
                StreetAddress streetAddress = run.query(sql, new StreetAddressHandler());
                return streetAddress;
            }
        }
        catch (SQLException ex){
            logger.warn(ex.getMessage());
        }
        return null;
    }

    /**
     * Retrieves a list of street names that are contained within the supplied zipcode
     * @param zip5
     * @return List<String>
     */
    public List<String> getStreetsInZip(String zip5)
    {
        String sql =
                "SELECT DISTINCT featnames.fullname FROM addr \n" +
                "JOIN featnames ON addr.tlid = featnames.tlid \n" +
                "WHERE zip = ? \n" +
                "ORDER BY featnames.fullname";
        try {
            return run.query(sql, new StreetListHandler(), zip5);
        }
        catch (SQLException ex){
            logger.error(ex.getMessage());
        }
        return null;
    }

    public List<Line> getStreetLineGeometry(String streetName, List<String> zip5List)
    {
        if (zip5List == null || zip5List.isEmpty()) return null; // short circuit
        String sql =
                "WITH streets AS (\n" +
                "  SELECT * FROM tiger_data.ny_edges edges\n" +
                "  WHERE fullname ILIKE ? AND (%s)\n" +
                ")\n" +
                "SELECT fullname, " +
                        "ST_AsGeoJson(\n" +
                            "ST_LineMerge(\n" +
                                "(SELECT ST_Union(the_geom) FROM streets)\n" +
                            ")\n" +
                        ") AS lines \n" +
                "FROM streets\n" +
                "GROUP BY fullname";
        List<String> zip5WhereList = new ArrayList<>();
        for (String zip5 : zip5List) {
            zip5WhereList.add(String.format("(zipl = '%s' OR zipr = '%s')", zip5, zip5));
        }
        String zip5Where = (!zip5WhereList.isEmpty()) ? StringUtils.join(zip5WhereList, " OR ") : "FALSE";
        sql = String.format(sql, zip5Where);
        try {
            return run.query(sql, new StreetLineGeometryHandler(), streetName);
        }
        catch (SQLException ex) {
            logger.error("Failed to retrieve street line geometry!", ex);
        }
        return null;
    }

    /** Converts the geocode result set into a GeocodedStreetAddress */
    public static class GeocodedStreetAddressHandler implements ResultSetHandler<GeocodedStreetAddress>
    {
        @Override
        public GeocodedStreetAddress handle(ResultSet rs) throws SQLException
        {
            StreetAddress streetAddress = new StreetAddressHandler().handle(rs);
            if (streetAddress != null) {
                Geocode geocode = new Geocode(new Point(rs.getDouble("lat"), rs.getDouble("lon")), null, TigerGeocoderDao.class.getSimpleName());
                geocode.setRawQuality(rs.getInt("rating"));
                return new GeocodedStreetAddress(streetAddress, geocode);
            }
            return null;
        }
    }

    /** Converts result into a list of street names */
    public static class StreetListHandler implements ResultSetHandler<ArrayList<String>>
    {
        @Override
        public ArrayList<String> handle(ResultSet rs) throws SQLException
        {
            ArrayList<String> streets = new ArrayList<>();
            while (rs.next()){
                streets.add(rs.getString(1));
            }
            return streets;
        }
    }

    /** Converts the parsed address result set into a StreetAddress */
    private static class StreetAddressHandler implements ResultSetHandler<StreetAddress>
    {
        private static Map<String, String> saColumns = new HashMap<>();
        private static BeanProcessor rowProcessor;
        static
        {
            saColumns.put("address", "bldgNum");
            saColumns.put("predirabbrev", "preDir");
            saColumns.put("streetname", "streetName");
            saColumns.put("streettypeabbrev", "streetType");
            saColumns.put("postdirabbrev", "postDir");
            saColumns.put("internal", "internal");
            saColumns.put("location", "location");
            saColumns.put("stateabbrev", "state");
            saColumns.put("zip", "zip5");
            rowProcessor = new BeanProcessor(saColumns);
        }

        @Override
        public StreetAddress handle(ResultSet rs) throws SQLException
        {
            return new BeanHandler<>(StreetAddress.class, new BasicRowProcessor(rowProcessor)).handle(rs);
        }
    }

    private static class StreetLineGeometryHandler implements ResultSetHandler<List<Line>> {
        @Override
        public List<Line> handle(ResultSet rs) throws SQLException {
            while (rs.next()) {
                String lines= rs.getString("lines");
                if (lines != null) {
                    return getLinesFromJson(lines);
                }
            }
            return null;
        }
    }

    private static List<Line> getLinesFromJson(String jsonLines)
    {
        if (jsonLines != null && !jsonLines.isEmpty() && jsonLines != "null") {
            logger.debug("jsonLines: " + jsonLines);
            List<Line> lines = new ArrayList<>();
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                JsonNode mapNode = objectMapper.readTree(jsonLines);
                String type = mapNode.get("type").asText();
                GeometryTypes geoType = GeometryTypes.valueOf(type.toUpperCase());

                JsonNode coordinates = mapNode.get("coordinates");
                if (geoType.equals(GeometryTypes.LINESTRING)) {
                    List<Point> points = new ArrayList<>();
                    for (int i = 0; i < coordinates.size(); i++) {
                        points.add(new Point(coordinates.get(i).get(1).asDouble(), coordinates.get(i).get(0).asDouble()));
                    }
                    lines.add(new Line(points));
                }
                else if (geoType.equals(GeometryTypes.MULTILINESTRING)) {
                    for (int i = 0; i < coordinates.size(); i++) {
                        List<Point> points = new ArrayList<>();
                        JsonNode jsonLine = coordinates.get(i);
                        for (int j = 0; j < jsonLine.size(); j++) {
                            points.add(new Point(jsonLine.get(j).get(1).asDouble(), jsonLine.get(j).get(0).asDouble()));
                        }
                        lines.add(new Line(points));
                    }
                }
                else {
                    return null;
                }
                return lines;
            }
            catch (IOException ex) {
                logger.error(ex);
            }
        }
        return null;
    }

}