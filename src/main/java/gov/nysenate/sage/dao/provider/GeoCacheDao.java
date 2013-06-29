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
import org.apache.commons.lang3.text.WordUtils;
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
    protected static int BUFFER_SIZE;
    protected QueryRunner tigerRun = getTigerQueryRunner();

    public GeoCacheDao() {
        this.config = ApplicationFactory.getConfig();
        BUFFER_SIZE = Integer.parseInt(this.config.getValue("geocache.buffer.size", "100"));
    }

    /**
     * Performs a lookup on the cache table and returns a GeocodedStreetAddress upon match.
     * @param sa  StreetAddress to lookup
     * @return    GeocodedStreetAddress
     */
    public GeocodedStreetAddress getCacheHit(StreetAddress sa)
    {
        logger.debug("Looking up " + sa.toStringParsed() + " in cache..");
        if (isStreetAddressRetrievable(sa)) {
            String sql = "SELECT gc.*, ST_Y(latlon) AS lat, ST_X(latlon) AS lon\n" +
                         "FROM cache.geocache AS gc \n";
            if (!sa.isPoBoxAddress() && !sa.isStreetEmpty()) {
                sql += "WHERE gc.bldgnum = ? \n" +
                        "AND COALESCE(gc.predir, '') = ? \n" +
                        "AND gc.street = ? \n" +
                        "AND COALESCE(gc.postdir, '') = ? \n" +
                        "AND gc.streetType = ? \n" +
                        "AND gc.state = ? \n" +
                        "AND ((gc.zip5 = ? AND gc.zip5 != '') OR (? = '' AND gc.location = ? AND gc.location != ''))";
                try {
                    return tigerRun.query(sql, new GeocodedStreetAddressHandler(),
                            sa.getBldgNum(), sa.getPreDir(), sa.getStreetName(), sa.getPostDir(),
                            sa.getStreetType(), sa.getState(), sa.getZip5(), sa.getZip5(), sa.getLocation());
                }
                catch (SQLException ex) {
                    logger.error("Error retrieving geo cache hit!", ex);
                }
            }
            /** PO BOX addresses can be looked up by just the location/zip */
            else {
                logger.debug("Cache lookup without street");
                sql += "WHERE gc.state = ? \n" +
                       "AND gc.street = '' \n" +
                       "AND ((gc.zip5 = ? AND gc.zip5 != '') OR (? = '' AND gc.location = ? AND gc.location != ''))";
                try {
                    return tigerRun.query(sql, new GeocodedStreetAddressHandler(), sa.getState(),
                            sa.getZip5(), sa.getZip5(), sa.getLocation());
                }
                catch (SQLException ex) {
                    logger.error("Error retrieving geo cache hit!", ex);
                }
            }
        }
        return null;
    }

    /**
     * Pushes a geocoded address to the buffer for saving to cache.
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
     * Pushes a list of geocoded addresses to the buffer for saving to cache.
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
     * Saves any GeocodedAddress objects stored in the buffer into the database. The address is parsed into
     * a StreetAddress object so that look-up is more reliable given variations in the address.
     */
    public synchronized void flushCacheBuffer()
    {
        String sql = "INSERT INTO cache.geocache (bldgnum, predir, street, streettype, postdir, location, state, zip5," +
                                                " latlon, method, quality, zip4) " +
                     "VALUES (?,?,?,?,?,?,?,?,ST_GeomFromText(?),?,?,?)";

        while (!cacheBuffer.isEmpty()) {
            GeocodedAddress geocodedAddress = cacheBuffer.remove();

            if (geocodedAddress != null && geocodedAddress.isValidAddress() && geocodedAddress.isValidGeocode()) {
                Address address = geocodedAddress.getAddress();
                Geocode gc = geocodedAddress.getGeocode();
                StreetAddress sa = StreetAddressParser.parseAddress(address);
                if (getCacheHit(sa) == null) {
                    if (isCacheableStreetAddress(sa)) {
                        try {
                            tigerRun.update(sql, Integer.valueOf(sa.getBldgNum()),
                                    sa.getPreDir(), sa.getStreetName(), sa.getStreetType(), sa.getPostDir(), sa.getLocation(),
                                    sa.getState(), sa.getZip5(), "POINT(" + gc.getLon() + " " + gc.getLat() + ")",
                                    gc.getMethod(), gc.getQuality().name(), sa.getZip4());
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
     * Retrieves a GeocodedStreetAddress from the result set. This is the parsed format used for look-ups.
     */
    public class GeocodedStreetAddressHandler implements ResultSetHandler<GeocodedStreetAddress>
    {
        @Override
        public GeocodedStreetAddress handle(ResultSet rs) throws SQLException {
            if (rs.next()) {
                StreetAddress sa = new StreetAddress();
                sa.setBldgNum(rs.getInt("bldgnum"));
                sa.setPreDir(rs.getString("predir"));
                sa.setStreetName(WordUtils.capitalizeFully(rs.getString("street")));
                sa.setStreetType(WordUtils.capitalizeFully(rs.getString("streettype")));
                sa.setPostDir(rs.getString("postdir"));
                sa.setLocation(WordUtils.capitalizeFully(rs.getString("location")));
                sa.setState(rs.getString("state"));
                sa.setZip5(rs.getString("zip5"));
                sa.setZip4(rs.getString("zip4"));
                Geocode gc = getGeocodeFromResultSet(rs);
                return new GeocodedStreetAddress(sa, gc);
            }
            return null;
        }
    }

    /**
     * Constructs a Geocode from the result set.
     * @param rs    Result set that has rs.next() already called
     * @throws SQLException
     */
    private Geocode getGeocodeFromResultSet(ResultSet rs) throws SQLException
    {
        if (rs != null) {
            Geocode gc = new Geocode();
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
                return gc;
            }
            catch (IllegalArgumentException ex) {
                gc.setQuality(GeocodeQuality.UNKNOWN);
            }
        }
        return null;
    }

    /**
     * Determines if street address is cache-able. The goal is to cache unique street level addresses and
     * unique (location/zip only) addresses. The location/zip only addresses allow for caching PO BOX type
     * addresses where the geocode is likely going to be that of the (city, state, zip).
     * @param sa
     * @return
     */
    private boolean isCacheableStreetAddress(StreetAddress sa)
    {
        return (!sa.getState().isEmpty() &&
               (!sa.getStreet().isEmpty() && !sa.getStreet().startsWith("[") && sa.getBldgNum() > 0)
                 ||(sa.getStreet().isEmpty() && sa.getBldgNum() == 0
                    && (!sa.getLocation().isEmpty() || !sa.getZip5().isEmpty())));
    }

    private boolean isStreetAddressRetrievable(StreetAddress sa)
    {
        return isCacheableStreetAddress(sa);
    }
}
