package gov.nysenate.sage.dao.provider;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.address.GeocodedStreetAddress;
import gov.nysenate.sage.model.address.StreetAddress;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;
import gov.nysenate.sage.util.Config;
import gov.nysenate.sage.util.StreetAddressParser;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class GeoCacheDao extends BaseDao
{
    protected Logger logger = Logger.getLogger(GeoCacheDao.class);
    protected Config config;
    protected static TigerGeocoderDao tigerGeocoderDao = new TigerGeocoderDao();
    protected static BlockingQueue<GeocodedAddress> cacheBuffer = new LinkedBlockingQueue<>();
    protected static int BUFFER_SIZE = 100;
    protected QueryRunner tigerRun = getTigerQueryRunner();

    public GeoCacheDao() {
        this.config = ApplicationFactory.getConfig();
        BUFFER_SIZE = Integer.parseInt(this.config.getValue("cache.buffer.size", "100"));
    }

    /**
     *
     * @param address
     * @return
     */
    public GeocodedStreetAddress getCacheHit(Address address)
    {
        String sql =
                "SELECT gc.*, ST_Y(latlon) AS lat, ST_X(latlon) AS lon\n" +
                "FROM cache.geocache AS gc, tiger.normalize_address(?) AS sa\n" +
                "WHERE gc.bldgnum = sa.address\n" +
                "AND COALESCE(gc.predir, '') ILIKE COALESCE(sa.predirabbrev, '')\n" +
                "AND gc.street ILIKE sa.streetname\n" +
                "AND COALESCE(gc.postdir, '') ILIKE COALESCE(sa.postdirabbrev, '')\n" +
                "AND gc.streetType ILIKE sa.streettypeabbrev\n" +
                "AND gc.zip5 ILIKE sa.zip";
        try {
            return tigerRun.query(sql, new GeocodedStreetAddressHandler(), address.toNormalizedString());
        }
        catch (SQLException ex) {
            logger.error("Error retrieving geo cache hit!", ex);
        }
        return null;
    }

    public boolean isCached(Address address)
    {
        String sql =
                "SELECT 1 FROM cache.geocache AS gc, tiger.normalize_address(?) AS sa\n" +
                "WHERE gc.bldgnum = sa.address\n" +
                "AND COALESCE(gc.predir, '') ILIKE COALESCE(sa.predirabbrev, '')\n" +
                "AND gc.street ILIKE sa.streetname\n" +
                "AND COALESCE(gc.postdir, '') ILIKE COALESCE(sa.postdirabbrev, '')\n" +
                "AND gc.streetType ILIKE sa.streettypeabbrev\n" +
                "AND gc.zip5 ILIKE sa.zip";
        try {
            return tigerRun.query(sql, new ResultSetHandler<Boolean>() {
                @Override
                public Boolean handle(ResultSet rs) throws SQLException {
                    if (rs.next()) {
                        return (rs.getInt(1) == 1);
                    }
                    return false;
                }
            }, address.toNormalizedString());
        }
        catch (SQLException ex) {
            logger.error("Error retrieving geo cache hit!", ex);
        }
        return false;
    }

    /**
     *
     * @param geocodedAddress
     */
    public void cacheGeocodedAddress(GeocodedAddress geocodedAddress)
    {
        cacheBuffer.add(geocodedAddress);
        if (cacheBuffer.size() > BUFFER_SIZE) {
            flushCacheBuffer();
        }
    }

    /**
     *
     * @param geocodedAddresses
     */
    public void cacheGeocodedAddresses(List<GeocodedAddress> geocodedAddresses)
    {
        cacheBuffer.addAll(geocodedAddresses);
        if (cacheBuffer.size() > BUFFER_SIZE) {
            flushCacheBuffer();
        }
    }

    /**
     *
     */
    public synchronized void flushCacheBuffer()
    {
        String sql = "INSERT INTO cache.geocache (bldgnum, predir, street, streettype, postdir, location, state, zip5," +
                                                " latlon, method, quality) " +
                     "VALUES (?,?,?,?,?,?,?,?,ST_GeomFromText(?),?,?)";

        while (!cacheBuffer.isEmpty()) {
            GeocodedAddress geocodedAddress = cacheBuffer.remove();
            if (geocodedAddress != null && geocodedAddress.isAddressValid() && geocodedAddress.isGeocoded()) {
                Address address = geocodedAddress.getAddress();
                Geocode gc = geocodedAddress.getGeocode();
                if (getCacheHit(address) == null) {
                    StreetAddress sa = StreetAddressParser.parseAddress(geocodedAddress.getAddress());
                    if (sa.getBldgNum() > 0 && sa.getStreet() != null && !sa.getStreet().startsWith("[")) {
                        try {
                            tigerRun.update(sql, Integer.valueOf(sa.getBldgNum()),
                                    sa.getPreDir(), sa.getStreet(), sa.getStreetType(), sa.getPostDir(), sa.getLocation(),
                                    sa.getState(), sa.getZip5(), "POINT(" + gc.getLon() + " " + gc.getLat() + ")",
                                    gc.getMethod(), gc.getQuality().name());
                            logger.info("Saved " + sa.toString() + " in cache.");
                        }
                        catch(SQLException ex) {
                            logger.trace(ex); // Most likely a duplicate row warning
                        }
                        catch(Exception ex) {
                            logger.error(ex);
                        }
                    }
                }
                else {
                    logger.debug(address + " already in cache.");
                }
            }
        }
    }

    /**
     *
     */
    public class GeocodedStreetAddressHandler implements ResultSetHandler<GeocodedStreetAddress>
    {
        @Override
        public GeocodedStreetAddress handle(ResultSet rs) throws SQLException {
            if (rs.next()) {
                StreetAddress sa = new StreetAddress();
                Geocode gc = new Geocode();
                sa.setBldgNum(rs.getInt("bldgnum"));
                sa.setPreDir(rs.getString("predir"));
                sa.setStreet(rs.getString("street"));
                sa.setStreetType(rs.getString("streettype"));
                sa.setPostDir(rs.getString("postdir"));
                sa.setLocation(rs.getString("location"));
                sa.setState(rs.getString("state"));
                sa.setZip5(rs.getString("zip5"));
                gc.setLat(rs.getDouble("lat"));
                gc.setLon(rs.getDouble("lon"));
                gc.setMethod(rs.getString("method"));
                try {
                    if (rs.getString("quality") != null) {
                        gc.setQuality(GeocodeQuality.valueOf(rs.getString("quality").toUpperCase()));
                    }
                    else {
                        gc.setQuality(GeocodeQuality.UNKNOWN);
                    }
                }
                catch (IllegalArgumentException ex) {
                    gc.setQuality(GeocodeQuality.UNKNOWN);
                }
                return new GeocodedStreetAddress(sa, gc);
            }
            return null;
        }
    }

}
