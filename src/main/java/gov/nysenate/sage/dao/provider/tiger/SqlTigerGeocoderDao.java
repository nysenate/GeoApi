package gov.nysenate.sage.dao.provider.tiger;

import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedStreetAddress;
import gov.nysenate.sage.model.address.StreetAddress;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.Line;
import gov.nysenate.sage.model.geo.Point;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.BeanProcessor;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Provides a query interface to the TigerGeocoder database. For documentation on the geocoder
 * and it's available commands, refer to the following documentation (link subject to change):
 *
 * http://postgis.net/docs/Extras.html
 */
@Repository
public class SqlTigerGeocoderDao implements TigerGeocoderDao
{
    private static Logger logger = LoggerFactory.getLogger(SqlTigerGeocoderDao.class);
    private Environment env;
    private QueryRunner run;
    private int GEOCODER_TIMEOUT = 15000; //ms
    private BaseDao baseDao;

    @Autowired
    public SqlTigerGeocoderDao(Environment env, BaseDao baseDao) {
        this.baseDao = baseDao;
        run = this.baseDao.getTigerQueryRunner();
        this.env = env;
        GEOCODER_TIMEOUT = env.getTigerGeocoderTimeout();
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
                     "FROM geocode(:address, 1) AS g;";
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("address", address.toString());

            List<GeocodedStreetAddress> geocodedStreetAddressList =
                    this.baseDao.geoApiNamedJbdcTemaplate.query(sql, params, new GeocodedStreetAddressHandler());

            if (geocodedStreetAddressList.size() > 0 && geocodedStreetAddressList.get(0) != null) {
                return geocodedStreetAddressList.get(0);
            }
        }
        catch (Exception ex){
            logger.warn(ex.getMessage());
        }
        finally {
            this.baseDao.closeConnection(conn);
        }

        return geoStreetAddress;
    }

    public GeocodedStreetAddress getGeocodedStreetAddress(Address address)
    {
        return getGeocodedStreetAddress(this.baseDao.getTigerConnection(), address);
    }

    /**
     * This method may be used to parse an Address into it's street address components using
     * Tiger Geocoder's built in address parser.
     * @param address   Address to parse
     * @return          Street Address containing the parsed components
     */
    public StreetAddress getStreetAddress(Address address)
    {
        String sql = "SELECT * FROM normalize_address(:address)";
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("params", address.toNormalizedString());

            List<StreetAddress> streetAddressList =
                    this.baseDao.geoApiNamedJbdcTemaplate.query(sql, params ,new StreetAddressHandler());

            if (streetAddressList.size() > 0 && streetAddressList.get(0) != null) {
                return streetAddressList.get(0);
            }
        }
        catch (Exception ex){
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
                List<StreetAddress> streetAddressList =
                        this.baseDao.geoApiNamedJbdcTemaplate.query(sql, new StreetAddressHandler());
                if (streetAddressList.size() > 0 && streetAddressList.get(0) != null) {
                    return streetAddressList.get(0);
                }
            }
        }
        catch (Exception ex){
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
                "WHERE zip = :zip5 \n" +
                "ORDER BY featnames.fullname";
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("zip5", zip5);

            return this.baseDao.geoApiNamedJbdcTemaplate.query(sql, params , new StreetListHandler());
        }
        catch (Exception ex){
            logger.error(ex.getMessage());
        }
        return null;
    }

    /**
     * Retrieves JSON geometry for a street that is matched in the set of zip5s.
     * @param streetName
     * @param zip5List
     * @return GeoJSON string or null if no match.
     */
    public String getStreetLineGeometryAsJson(String streetName, List<String> zip5List) {
        if (zip5List == null || zip5List.isEmpty()) return null; // short circuit
        String sql =
                "WITH streets AS (\n" +
                "  SELECT * FROM tiger_data.ny_edges edges\n" +
                "  WHERE fullname ILIKE :streetName AND (%s)\n" +
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
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("streetName", streetName);

            List<String> zip5QueryList =  this.baseDao.geoApiNamedJbdcTemaplate.query(sql, params, new zip5Handler());
            if (zip5QueryList.size() > 0 && zip5QueryList.get(0) != null) {
                return zip5QueryList.get(0);
            }
        }
        catch (Exception ex) {
            logger.error("Failed to retrieve street line geometry!", ex);
        }
        return null;
    }

    /**
     * Retrieves a collection of line objects by processing the result of getStreetLineGeometryAsJson().
     * @param streetName
     * @param zip5List
     * @return
     */
    public List<Line> getStreetLineGeometry(String streetName, List<String> zip5List)
    {
        String streetLineJson = getStreetLineGeometryAsJson(streetName, zip5List);
        if (streetLineJson != null) {
            return this.baseDao.getLinesFromJson(streetLineJson);
        }
        return null;
    }

    /** Converts result into a list of street names */
    public static class StreetListHandler implements RowMapper<String>
    {
        @Override
        public String mapRow(ResultSet rs, int rowNum) throws SQLException
        {
            return rs.getString(1);
        }
    }

    /** Converts the parsed address result set into a StreetAddress */
    private static class StreetAddressHandler implements RowMapper<StreetAddress> {
        @Override
        public StreetAddress mapRow(ResultSet rs, int rowNum) throws SQLException {
            StreetAddress streetAddress = new StreetAddress();

            streetAddress.setBldgNum(rs.getInt("bldgNum"));
            streetAddress.setPreDir(rs.getString("preDir"));
            streetAddress.setStreetName(rs.getString("streetName"));
            streetAddress.setStreetType(rs.getString("streetType"));
            streetAddress.setPostDir(rs.getString("postDir"));
            streetAddress.setInternal(rs.getString("internal"));
            streetAddress.setLocation(rs.getString("location"));
            streetAddress.setState(rs.getString("state"));
            streetAddress.setZip5(rs.getString("zip5"));

            return streetAddress;
        }
    }

    /** Converts the geocode result set into a GeocodedStreetAddress */
    private static class GeocodedStreetAddressHandler implements RowMapper<GeocodedStreetAddress> {
        @Override
        public GeocodedStreetAddress mapRow(ResultSet rs, int rowNum) throws SQLException {
            StreetAddress streetAddress = new StreetAddressHandler().mapRow(rs, rowNum);
            if (streetAddress != null) {
                Geocode geocode = new Geocode(new Point(rs.getDouble("lat"), rs.getDouble("lon")), null,
                        SqlTigerGeocoderDao.class.getSimpleName());
                geocode.setRawQuality(rs.getInt("rating"));
                return new GeocodedStreetAddress(streetAddress, geocode);
            }
            return null;
        }
    }

    private static class zip5Handler implements RowMapper<String> {
        @Override
        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getString("zip5");
        }
    }

}