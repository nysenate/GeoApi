package gov.nysenate.sage.dao.provider.tiger;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedStreetAddress;
import gov.nysenate.sage.model.address.StreetAddress;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.Line;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.scripts.streetfinder.model.AddressWithoutNum;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides a query interface to the TigerGeocoder database. For documentation on the geocoder
 * and it's available commands, refer to the following documentation (link subject to change):
 * <a href="http://postgis.net/docs/Extras.html">...</a>
 */
@Repository
public class SqlTigerGeocoderDao implements TigerGeocoderDao {
    private static final Logger logger = LoggerFactory.getLogger(SqlTigerGeocoderDao.class);
    private final BaseDao baseDao;

    @Autowired
    public SqlTigerGeocoderDao(BaseDao baseDao) {
        this.baseDao = baseDao;
    }

    /** {@inheritDoc} */
    public GeocodedStreetAddress getGeocodedStreetAddress(Address address) {
        GeocodedStreetAddress geoStreetAddress = null;
        String sql = "SELECT g.rating, ST_Y(geomout) As lat, ST_X(geomout) As lon, (addy).* \n" +
                     "FROM geocode(:address, 1) AS g";
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("address", address.toString());

            List<GeocodedStreetAddress> geocodedStreetAddressList =
                    this.baseDao.tigerNamedJdbcTemplate.query(sql, params, new GeocodedStreetAddressHandler());

            if (geocodedStreetAddressList.isEmpty()) {
                return null;
            }

            return geocodedStreetAddressList.get(0);
        }
        catch (Exception ex){
            logger.warn(ex.getMessage());
        }
        return geoStreetAddress;
    }

    /** {@inheritDoc} */
    public StreetAddress getStreetAddress(Point point) {
        String sql = "SELECT (addy[1]).* " +
                     "FROM reverse_geocode(ST_GeomFromText('POINT(" + point.getLon() + " " + point.getLat() + ")',4269),true) As r;";
        try {
            List<StreetAddress> streetAddressList =
                    this.baseDao.tigerNamedJdbcTemplate.query(sql, new StreetAddressHandler());
            if (!streetAddressList.isEmpty() && streetAddressList.get(0) != null) {
                return streetAddressList.get(0);
            }
        }
        catch (Exception ex) {
            logger.warn(ex.getMessage());
        }
        return null;
    }

    /** {@inheritDoc} */
    public List<String> getStreetsInZip(String zip5) {
        String sql =
                "SELECT DISTINCT featnames.fullname FROM addr \n" +
                "JOIN featnames ON addr.tlid = featnames.tlid \n" +
                "WHERE zip = :zip5 \n" +
                "ORDER BY featnames.fullname";
        try {
            var params = new MapSqlParameterSource("zip5", zip5);
            return this.baseDao.tigerNamedJdbcTemplate.query(sql, params , new StreetListHandler());
        }
        catch (Exception ex) {
            logger.error(ex.getMessage());
        }
        return null;
    }

    /** {@inheritDoc} */
    public String getStreetLineGeometryAsJson(String streetName, List<Integer> zip5List) {
        if (zip5List == null || zip5List.isEmpty()) {
            return null;
        }
        String sql =
                """
                        WITH streets AS (
                          SELECT * FROM tiger_data.ny_edges edges
                          WHERE fullname ILIKE :streetName AND (%s)
                        )
                        SELECT fullname, \
                        ST_AsGeoJson(
                        ST_LineMerge(
                        (SELECT ST_Union(the_geom) FROM streets)
                        )
                        ) AS lines\s
                        FROM streets
                        GROUP BY fullname""";
        List<String> zip5WhereList = new ArrayList<>();
        for (Integer zip5 : zip5List) {
            zip5WhereList.add(String.format("(zipl %s OR zipr %s)", zip5, zip5));
        }
        String zip5Where = StringUtils.join(zip5WhereList, " OR ");
        sql = String.format(sql, zip5Where);
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("streetName", streetName);

            List<String> zip5QueryList = this.baseDao.tigerNamedJdbcTemplate.query(sql, params, new LineHandler());
            if (!zip5QueryList.isEmpty() && zip5QueryList.get(0) != null) {
                return zip5QueryList.get(0);
            }
        }
        catch (Exception ex) {
            logger.error("Failed to retrieve street line geometry!", ex);
        }
        return null;
    }

    /** {@inheritDoc} */
    public List<Line> getStreetLineGeometry(String streetName, List<Integer> zip5List) {
        String streetLineJson = getStreetLineGeometryAsJson(streetName, zip5List);
        if (streetLineJson != null) {
            return BaseDao.getLinesFromJson(streetLineJson);
        }
        return null;
    }

    /** Converts result into a list of street names */
    public static class StreetListHandler implements RowMapper<String> {
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
            // TODO: reverse if highway
            // TODO: skip if "stateabbrev" not NY
            var street = StreetAddress.combine(" ", rs.getString("predirabbrev"), rs.getString("streetName"),
                    rs.getString("streettypeabbrev"), rs.getString("postdirabbrev"));
            var awn = new AddressWithoutNum(street, rs.getString("location"), rs.getInt("zip"));
            var streetAddress = new StreetAddress(awn);
            streetAddress.setBldgId(rs.getString("address"));
            streetAddress.setInternal(rs.getString("internal"));
            return streetAddress;
        }
    }

    /** Converts the geocode result set into a GeocodedStreetAddress */
    private static class GeocodedStreetAddressHandler implements RowMapper<GeocodedStreetAddress> {
        @Override
        public GeocodedStreetAddress mapRow(ResultSet rs, int rowNum) throws SQLException {
            StreetAddress streetAddress = new StreetAddressHandler().mapRow(rs, rowNum);
            var geocode = new Geocode(new Point(rs.getDouble("lat"), rs.getDouble("lon")), null,
                    SqlTigerGeocoderDao.class.getSimpleName());
            geocode.setRawQuality(rs.getInt("rating"));
            return new GeocodedStreetAddress(streetAddress, geocode);
        }
    }

    private static class LineHandler implements RowMapper<String> {
        @Override
        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getString("lines");
        }
    }
}