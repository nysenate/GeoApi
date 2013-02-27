package gov.nysenate.sage.dao;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedStreetAddress;
import gov.nysenate.sage.model.address.StreetAddress;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.util.FormatUtil;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.BeanProcessor;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private int GEOCODER_TIMEOUT = 3000;

    public GeocodedStreetAddress getGeocodedStreetAddress(Connection conn, Address address)
    {
        String sql = "SELECT g.rating, ST_Y(geomout) As lat, ST_X(geomout) As lon, (addy).* \n" +
                     "FROM geocode(?, 1) AS g;";
        try {
            setTimeOut(conn, run, GEOCODER_TIMEOUT);
            return run.query(conn, sql, new GeocodedStreetAddressHandler(), address.toString());
        }
        catch (SQLException ex){
            logger.warn(ex.getMessage());
        }
        return null;
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
            StreetAddress streetAddress = run.query(sql, new StreetAddressHandler(), address.toString());
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

    /** Converts the geocode result set into a GeocodedStreetAddress */
    public static class GeocodedStreetAddressHandler implements ResultSetHandler<GeocodedStreetAddress>
    {
        @Override
        public GeocodedStreetAddress handle(ResultSet rs) throws SQLException
        {
            StreetAddress streetAddress = new StreetAddressHandler().handle(rs);
            if (streetAddress != null) {
                Geocode geocode = new Geocode(new Point(rs.getDouble("lat"), rs.getDouble("lon")));
                geocode.setRawQuality(rs.getInt("rating"));
                return new GeocodedStreetAddress(streetAddress, geocode);
            }
            return null;
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
            saColumns.put("streetname", "street");
            saColumns.put("streettypeabbrev", "streetType");
            saColumns.put("postdirabbrev", "postDir");
            saColumns.put("internal", "internal");
            saColumns.put("location", "location");
            saColumns.put("stateabbrev", "state");
            saColumns.put("zip", "postal");
            rowProcessor = new BeanProcessor(saColumns);
        }

        @Override
        public StreetAddress handle(ResultSet rs) throws SQLException
        {
            return new BeanHandler<>(StreetAddress.class, new BasicRowProcessor(rowProcessor)).handle(rs);
        }
    }
}