package gov.nysenate.sage.dao;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedStreetAddress;
import gov.nysenate.sage.model.address.StreetAddress;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.Point;
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

public class TigerGeocoderDao extends BaseDao
{
    private static Logger logger = Logger.getLogger(TigerGeocoderDao.class);
    private QueryRunner run = getTigerQueryRunner();

    private GeocodedStreetAddress getGeocodedStreetAddress(Connection conn, Address address)
    {
        String sql = "SELECT g.rating, ST_Y(geomout) As lat, ST_X(geomout) As lon, (addy).* \n" +
                     "FROM geocode(?, 1) AS g;";
        try {
            GeocodedStreetAddress geocodedStreetAddress = run.query(conn, sql, new GeocodedStreetAddressHandler(), address.toString());
            return geocodedStreetAddress;
        }
        catch (SQLException ex){
            logger.error(ex.getMessage());
        }
        return null;
    }

    public GeocodedStreetAddress getGeocodedStreetAddress(Address address)
    {
        return getGeocodedStreetAddress(this.getTigerConnection(), address);
    }

    public List<GeocodedStreetAddress> getGeocodedStreetAddresses(List<Address> addresses)
    {
        Connection conn = getTigerConnection();
        List<GeocodedStreetAddress> geocodedStreetAddresses = new ArrayList<>();
        for (Address address : addresses){
            geocodedStreetAddresses.add(getGeocodedStreetAddress(conn, address));
        }
        return geocodedStreetAddresses;
    }

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
            saColumns.put("address", "streetNum");
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

    private void setQueryTimeOut(int timeOut)
    {
        String sql = "set statement_timeout ?";
        try {
            run.update(sql, timeOut);
        }
        catch (SQLException ex){
            logger.error("Failed to set timeout! " + ex.getMessage());
        }
    }
}