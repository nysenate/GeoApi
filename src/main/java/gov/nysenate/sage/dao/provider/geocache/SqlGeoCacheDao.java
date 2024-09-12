package gov.nysenate.sage.dao.provider.geocache;

import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.address.GeocodedStreetAddress;
import gov.nysenate.sage.model.address.StreetAddress;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;
import gov.nysenate.sage.scripts.streetfinder.model.AddressWithoutNum;
import gov.nysenate.sage.util.StreetAddressParser;
import gov.nysenate.sage.util.TimeUtil;
import org.apache.commons.lang3.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Repository
public class SqlGeoCacheDao implements GeoCacheDao {
    private static final Logger logger = LoggerFactory.getLogger(SqlGeoCacheDao.class);
    private static final BlockingQueue<GeocodedAddress> cacheBuffer = new LinkedBlockingQueue<>();
    private static int BUFFER_SIZE = 100;
    private final BaseDao baseDao;

    @Autowired
    public SqlGeoCacheDao(Environment env, BaseDao baseDao) {
        this.baseDao = baseDao;
        BUFFER_SIZE = env.getGeocahceBufferSize();
    }

    /**
     * SQL Fragments for method getCacheHit(StreetAddress).
     */
    private final static String SQLFRAG_SELECT =
        "SELECT gc.*, ST_Y(latlon) AS lat, ST_X(latlon) AS lon \n" +
        "FROM cache.geocache AS gc";

    private final static String SQLFRAG_WITHOUT_ZIP =
    "WHERE gc.bldgnum = ? \n" +
    "AND gc.street = ? \n" +
    "AND gc.location = ? \n";

    private final static String SQLFRAG_WITH_ZIP =
    "AND gc.zip5 = ? \n";
//    "AND gc.zip4 = ? \n";

    private final static String SQL_CACHE_HIT_FULL_WITHOUT_ZIP =
            String.format("%s\n%s\nLIMIT 1", SQLFRAG_SELECT, SQLFRAG_WITHOUT_ZIP);

    private final static String SQL_CACHE_HIT_FULL_WITH_ZIP =
            String.format("%s\n%s\n%s\nLIMIT 1", SQLFRAG_SELECT, SQLFRAG_WITHOUT_ZIP, SQLFRAG_WITH_ZIP);

    /** {@inheritDoc} */
    public GeocodedStreetAddress getCacheHit(StreetAddress sa) {
        if (logger.isTraceEnabled()) {
            logger.trace("Looking up {} in cache...", sa);
        }
        if (isCacheableStreetAddress(sa)) {
            boolean buildingMatch = (!sa.isPoBoxAddress() && sa.hasStreet());
            try {
                if (sa.getZip5() == null) {
                    //without zip
                    return baseDao.tigerJbdcTemplate.query(SQL_CACHE_HIT_FULL_WITHOUT_ZIP,
                            new GeocodedStreetAddressHandler(buildingMatch),
                            sa.getBldgId(), sa.getStreet(), sa.getPostalCity());
                }
                else {
                    //with zip
                    return baseDao.tigerJbdcTemplate.query(SQL_CACHE_HIT_FULL_WITH_ZIP,
                            new GeocodedStreetAddressHandler(buildingMatch),
                            sa.getBldgId(), sa.getStreet(), sa.getPostalCity(), sa.getZip5());
                }
            }
            catch (Exception ex) {
                logger.error("Error retrieving geo cache hit!", ex);
            }
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Address {} is not retrievable", sa);
        }
        return null;
    }

    /** {@inheritDoc} */
    public void cacheGeocodedAddress(GeocodedAddress geocodedAddress) {
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

    /** {@inheritDoc} */
    public void cacheGeocodedAddresses(List<GeocodedAddress> geocodedAddresses) {
        if (geocodedAddresses != null) {
            for (GeocodedAddress geocodedAddress : geocodedAddresses) {
                cacheGeocodedAddress(geocodedAddress);
            }
        }
    }

    private final static String SQL_INSERT_CACHE_ENTRY =
            "INSERT INTO cache.geocache (bldgnum, street, location, zip5, latlon, method, quality, zip4) " +
                    "VALUES (:bldgnum, :street, :location, :zip5, ST_GeomFromText( :latlon ), :method, :quality, :zip4)";

    private final static String SQL_UPDATE_CACHE_ENTRY = "UPDATE cache.geocache " +
            "SET latlon = ST_GeomFromText(:latlon), method = :method, quality = :quality, zip4 = :zip4, updated = now() " +
            "WHERE bldgnum = :bldgnum AND street = :street AND zip5 = :zip5 AND location = :location";

    /**
     * Saves any GeocodedAddress objects stored in the buffer into the database. The address is parsed into
     * a StreetAddress object so that look-up is more reliable given variations in the address.
     */
    public synchronized void flushCacheBuffer() {
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
                            var params = new MapSqlParameterSource()
                                    .addValue("latlon", "POINT(" + gc.getLon() + " " + gc.getLat() + ")")
                                    .addValue("method", gc.getMethod())
                                    .addValue("quality", gc.getQuality().name())
                                    .addValue("zip4", sa.getZip4())
                                    .addValue("bldgnum", sa.getBldgId())
                                    .addValue("street", sa.getStreet())
                                    .addValue("zip5", sa.getZip5())
                                    .addValue("location", sa.getPostalCity());

                            int rowUpdated = baseDao.tigerNamedJdbcTemplate.update(SQL_UPDATE_CACHE_ENTRY, params);

                            if (logger.isTraceEnabled() && rowUpdated == 1) {
                                logger.trace("Number of rows updated = {}", rowUpdated);
                                logger.trace("Updated {} in cache.", sa);
                            }

                            if (rowUpdated == 0) {
                                var insertParams = new MapSqlParameterSource()
                                        .addValue("bldgnum", sa.getBldgId())
                                        .addValue("street", sa.getStreet())
                                        .addValue("location", sa.getPostalCity())
                                        .addValue("zip5", sa.getZip5())
                                        .addValue("zip4", sa.getZip4())
                                        .addValue("latlon", "POINT(" + gc.getLon() + " " + gc.getLat() + ")")
                                        .addValue("method", gc.getMethod())
                                        .addValue("quality", gc.getQuality().name());

                                int rowsInserted = baseDao.tigerNamedJdbcTemplate.update(SQL_INSERT_CACHE_ENTRY, insertParams);

                                if (logger.isTraceEnabled()) {
                                    logger.trace("Number of rows inserted = {}", rowsInserted);
                                    logger.trace("Inserted {} in cache.", sa);
                                }

                            }
                            else if (rowUpdated > 1) {
                                throw new IllegalStateException("Too many updates (" + rowUpdated + ") occurred for " + sa.toString());
                            }

                        }
                        catch (Exception e) {
                            logger.error("SQL EXCEPTION", e);
                        }
                    }
                }
            }
            if (startSize > 1) {
                logger.info("Cached {} geocodes in {} ms.", startSize, TimeUtil.getElapsedMs(startTime));
            }
        }
    }

    /**
     * Retrieves a GeocodedStreetAddress from the result set. This is the parsed format used for look-ups.
     * If the constructor is initialized with true, the result will be null if the geocode is not at least of HOUSE quality.
     * Otherwise the geocode quality won't be checked.
     */
    public class GeocodedStreetAddressHandler implements ResultSetExtractor<GeocodedStreetAddress> {
        private final boolean buildingMatch;

        public GeocodedStreetAddressHandler(boolean buildingMatch) {
            this.buildingMatch = buildingMatch;
        }

        @Override
        public GeocodedStreetAddress extractData(ResultSet rs) throws SQLException {
            if (rs.next()) {
                Geocode gc = getGeocodeFromResultSet(rs);
                if (gc == null || gc.getQuality() == null ||
                        (this.buildingMatch && gc.getQuality().compareTo(GeocodeQuality.HOUSE) < 0)) {
                    return null;
                }

                var awn = new AddressWithoutNum(WordUtils.capitalizeFully(rs.getString("street")),
                        WordUtils.capitalizeFully(rs.getString("location")), rs.getInt("zip5"));
                var sa = new StreetAddress(awn);
                sa.setBldgId(rs.getString("bldgnum"));
                sa.setZip4(rs.getInt("zip4"));
                return new GeocodedStreetAddress(sa, gc);
            }
            return null;
        }
    }

    /**
     * Constructs a Geocode from the result set.
     * @param rs    Result set that has rs.next() already called
     */
    private Geocode getGeocodeFromResultSet(ResultSet rs) throws SQLException {
        if (rs == null) {
            return null;
        }
        var gc = new Geocode();
        gc.setLat(rs.getDouble("lat"));
        gc.setLon(rs.getDouble("lon"));
        gc.setMethod(rs.getString("method"));
        gc.setCached(true);
        gc.encodeOpenLocationCode();
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

    /**
     * Determines if street address is cache-able. The goal is to cache unique street level addresses.
     * @return true if street address is cacheable.
     */
    private static boolean isCacheableStreetAddress(StreetAddress sa) {
        return !sa.getStreet().isEmpty() && !sa.getStreet().startsWith("[") && sa.getBldgId() != null;
    }
}
