package gov.nysenate.sage.dao.provider;

import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.address.GeocodedStreetAddress;
import gov.nysenate.sage.model.address.StreetAddress;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;
import gov.nysenate.sage.util.StreetAddressParser;
import gov.nysenate.sage.util.TimeUtil;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Repository
public class GeoCacheDao extends BaseDao
{
    private static Logger logger = LogManager.getLogger(GeoCacheDao.class);
    private static BlockingQueue<GeocodedAddress> cacheBuffer = new LinkedBlockingQueue<>();
    private static int BUFFER_SIZE = 100;
    private QueryRunner tigerRun = getTigerQueryRunner();
    private final Environment env;

    @Autowired
    public GeoCacheDao(Environment env) {
        this.env = env;
        BUFFER_SIZE = env.getGeocahceBufferSize();
    }

    /**
     * SQL Fragments for method getCacheHit(StreetAddress).
     */
    private final static String SQLFRAG_SELECT =
        "SELECT gc.*, ST_Y(latlon) AS lat, ST_X(latlon) AS lon \n" +
        "FROM cache.geocache AS gc";

    private final static String SQLFRAG_WHERE_FULL_MATCH =
        "WHERE gc.bldgnum = ? \n" +
            "AND gc.predir = ? \n" +
            "AND gc.street = ? \n" +
            "AND gc.postdir = ? \n" +
            "AND gc.streetType = ? \n" +
            // Zip level match
            "AND (gc.zip5 = ? AND gc.zip5 != '' AND gc.quality NOT IN ('CITY', 'UNKNOWN')\n" +
            // City (location) match when zip is empty
            "  OR ? = '' AND gc.zip5 = '' AND gc.location = ? AND gc.location != '' AND gc.state = ?)";

    private final static String SQL_CACHE_HIT_FULL = String.format("%s\n%s\nLIMIT 1", SQLFRAG_SELECT, SQLFRAG_WHERE_FULL_MATCH);

    /**
     * Performs a lookup on the cache table and returns a GeocodedStreetAddress upon match.
     * @param sa  StreetAddress to lookup
     * @return    GeocodedStreetAddress
     */
    public GeocodedStreetAddress getCacheHit(StreetAddress sa)
    {
        if (logger.isTraceEnabled()) {
            logger.trace("Looking up " + sa.toStringParsed() + " in cache..");
        }
        if (isStreetAddressRetrievable(sa)) {
            boolean buildingMatch = (!sa.isPoBoxAddress() && !sa.isStreetEmpty());
            try {
                return tigerRun.query(SQL_CACHE_HIT_FULL, new GeocodedStreetAddressHandler(buildingMatch),
                    sa.getBldgNum(), sa.getPreDir(), sa.getStreetName(), sa.getPostDir(),
                    sa.getStreetType(), sa.getZip5(), sa.getZip5(), sa.getLocation(), sa.getState());
            }
            catch (SQLException ex) {
                logger.error("Error retrieving geo cache hit!", ex);
            }
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Address " + sa.toStringParsed() + " is not retrievable");
        }
        return null;
    }

    /**
     * Pushes a geocoded address to the buffer for saving to cache.
     * @param geocodedAddress GeocodedAddress to cache.
     */
    public void cacheGeocodedAddress(GeocodedAddress geocodedAddress)
    {
        if (geocodedAddress != null && geocodedAddress.isValidAddress() && geocodedAddress.isValidGeocode()) {
            Geocode gc = geocodedAddress.getGeocode();
            if (!gc.isCached()) {
                cacheBuffer.add(geocodedAddress);
                if (cacheBuffer.size() > BUFFER_SIZE) {
                    flushCacheBuffer();
                }
            }
        }
    }

    /**
     * Pushes a list of geocoded addresses to the buffer for saving to cache.
     * @param geocodedAddresses GeocodedAddress List to cache.
     */
    public void cacheGeocodedAddresses(List<GeocodedAddress> geocodedAddresses)
    {
        if (geocodedAddresses != null) {
            for (GeocodedAddress geocodedAddress : geocodedAddresses) {
                cacheGeocodedAddress(geocodedAddress);
            }
        }
    }

    private final static String SQL_INSERT_CACHE_ENTRY =
        "INSERT INTO cache.geocache (bldgnum, predir, street, streettype, postdir, location, state, zip5, " +
                                    "latlon, method, quality, zip4) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ST_GeomFromText(?), ?, ?, ?)";

    private final static String SQL_UPDATE_CACHE_ENTRY = "update cache.geocache\n" +
            "set latlon = ST_GeomFromText(?), method = ?, quality = ?, zip4 = ?, updated = now()\n" +
            "where bldgnum = ?  and street = ? and streettype = ? and predir = ? and postdir = ?;";

    /**
     * Saves any GeocodedAddress objects stored in the buffer into the database. The address is parsed into
     * a StreetAddress object so that look-up is more reliable given variations in the address.
     */
    public synchronized void flushCacheBuffer()
    {
        if (!cacheBuffer.isEmpty()) {
            Timestamp startTime = TimeUtil.currentTimestamp();
            int startSize = cacheBuffer.size();

            while (!cacheBuffer.isEmpty()) {
                GeocodedAddress geocodedAddress = cacheBuffer.remove();
                if (geocodedAddress != null && geocodedAddress.isValidAddress() && geocodedAddress.isValidGeocode()) {
                    Address address = geocodedAddress.getAddress();
                    Geocode gc = geocodedAddress.getGeocode();
                    StreetAddress sa = StreetAddressParser.parseAddress(address);
                    if (isCacheableStreetAddress(sa)) {
                        try {
                            tigerRun.update(SQL_INSERT_CACHE_ENTRY, Integer.valueOf(sa.getBldgNum()),
                                sa.getPreDir(), sa.getStreetName(), sa.getStreetType(), sa.getPostDir(), sa.getLocation(),
                                sa.getState(), sa.getZip5(), "POINT(" + gc.getLon() + " " + gc.getLat() + ")",
                                gc.getMethod(), gc.getQuality().name(), sa.getZip4());
                            if (logger.isTraceEnabled()) {
                                logger.trace("Saved " + sa.toString() + " in cache.");
                            }
                        }
                        catch(SQLException ex) {
                            // Duplicate row warnings are expected sometimes and can be suppressed.
                            if (ex.getMessage().contains("duplicate key")) {
                                try {
                                    tigerRun.update(SQL_UPDATE_CACHE_ENTRY,
                                            "POINT(" + gc.getLon() + " " + gc.getLat() + ")",
                                            gc.getMethod(),
                                            gc.getQuality().name(),
                                            sa.getZip4(),

                                            sa.getBldgNum(),
                                            sa.getStreetName(),
                                            sa.getStreetType(),
                                            sa.getPreDir(),
                                            sa.getPostDir()
                                            );
                                    if (logger.isTraceEnabled()) {
                                        logger.trace("Saved " + sa.toString() + " in cache.");
                                    }
                                }
                                catch (SQLException e) {
                                    logger.info("SQL EXCEPTION WHILE UPDATE DUP");
                                    logger.warn(e.getMessage());
                                }
                            }
                            else {
                                logger.warn("NON UPDATE DUP SQL EXCEPTION");
                                logger.warn(ex.getMessage());
                            }
                        }
                        catch(Exception ex) {
                            logger.error(ex);
                        }
                    }
                }
            }
            if (startSize > 1) {
                logger.info(String.format("Cached %d geocodes in %d ms.", startSize, TimeUtil.getElapsedMs(startTime)));
            }
        }
    }

    /**
     * Retrieves a GeocodedStreetAddress from the result set. This is the parsed format used for look-ups.
     * If the constructor is initialized with true, the result will be null if the geocode is not of HOUSE quality.
     * Otherwise the geocode quality won't be checked.
     */
    public class GeocodedStreetAddressHandler implements ResultSetHandler<GeocodedStreetAddress>
    {
        private boolean buildingMatch;

        public GeocodedStreetAddressHandler(boolean buildingMatch) {
            this.buildingMatch = buildingMatch;
        }

        @Override
        public GeocodedStreetAddress handle(ResultSet rs) throws SQLException {
            if (rs.next()) {
                Geocode gc = getGeocodeFromResultSet(rs);
                if (gc == null || gc.getQuality() == null ||
                        (this.buildingMatch && gc.getQuality().compareTo(GeocodeQuality.HOUSE) < 0)) {
                    return null;
                }

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
            gc.setCached(true);
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
            return gc;
        }
        return null;
    }

    /**
     * Determines if street address is cache-able. The goal is to cache unique street level addresses and
     * unique (location/zip only) addresses. The location/zip only addresses allow for caching PO BOX type
     * addresses where the geocode is likely going to be that of the (city, state, zip).
     * @param sa StreetAddress
     * @return true if street address is cacheable.
     */
    private boolean isCacheableStreetAddress(StreetAddress sa)
    {
        return (!sa.getStreet().isEmpty() && !sa.getStreet().startsWith("[") && sa.getBldgNum() > 0)
               || (sa.getStreet().isEmpty() && sa.getBldgNum() == 0 &&
                  ((!sa.getLocation().isEmpty() && !sa.getState().isEmpty()) || !sa.getZip5().isEmpty()));
    }

    /**
     * Determines if the street address has enough data to be retrievable from cache.
     * @param sa StreetAddress
     * @return true if street address is retrievable.
     */
    private boolean isStreetAddressRetrievable(StreetAddress sa)
    {
        return isCacheableStreetAddress(sa);
    }
}
