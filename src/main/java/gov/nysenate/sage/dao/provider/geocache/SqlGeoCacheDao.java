package gov.nysenate.sage.dao.provider.geocache;

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
import org.apache.commons.lang3.text.WordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Repository
public class SqlGeoCacheDao implements GeoCacheDao
{
    private static Logger logger = LoggerFactory.getLogger(SqlGeoCacheDao.class);
    private static BlockingQueue<GeocodedAddress> cacheBuffer = new LinkedBlockingQueue<>();
    private static int BUFFER_SIZE = 100;
    private BaseDao baseDao;

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

    private final static String SQLFRAG_WITHOUT_ZIP =
    "WHERE gc.bldgnum = ? \n" +
    "AND gc.predir = ? \n" +
    "AND gc.street = ? \n" +
    "AND gc.postdir = ? \n" +
    "AND gc.streetType = ? \n" +
    "AND gc.location = ? \n" +
    "AND gc.state = ? \n";

    private final static String SQLFRAG_WITH_ZIP =
    "AND gc.zip5 = ? \n";
//    "AND gc.zip4 = ? \n";

    private final static String SQL_CACHE_HIT_FULL_WITHOUT_ZIP =
            String.format("%s\n%s\nLIMIT 1", SQLFRAG_SELECT, SQLFRAG_WITHOUT_ZIP);

    private final static String SQL_CACHE_HIT_FULL_WITH_ZIP =
            String.format("%s\n%s\n%s\nLIMIT 1", SQLFRAG_SELECT, SQLFRAG_WITHOUT_ZIP, SQLFRAG_WITH_ZIP);

    /** {@inheritDoc} */
    public GeocodedStreetAddress getCacheHit(StreetAddress sa)
    {
        if (logger.isTraceEnabled()) {
            logger.trace("Looking up " + sa.toStringParsed() + " in cache..");
        }
        if (isStreetAddressRetrievable(sa)) {
            boolean buildingMatch = (!sa.isPoBoxAddress() && !sa.isStreetEmpty());
            if (sa.getState().isEmpty()) {
                sa.setState("NY");
            }
            try {

                if (sa.getZip5().isEmpty()) {
                    //without zip
                    return baseDao.tigerJbdcTemplate.query(SQL_CACHE_HIT_FULL_WITHOUT_ZIP,
                            new GeocodedStreetAddressHandler(buildingMatch),
                            sa.getBldgNum(), sa.getPreDir(), sa.getStreetName(), sa.getPostDir(),
                            sa.getStreetType(), sa.getLocation(), sa.getState());
                }
                else {
                    //with zip
                    return baseDao.tigerJbdcTemplate.query(SQL_CACHE_HIT_FULL_WITH_ZIP,
                            new GeocodedStreetAddressHandler(buildingMatch),
                            sa.getBldgNum(), sa.getPreDir(), sa.getStreetName(), sa.getPostDir(),
                            sa.getStreetType(), sa.getLocation(), sa.getState(), sa.getZip5());
                }
            }
            catch (Exception ex) {
                logger.error("Error retrieving geo cache hit!", ex);
            }
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Address " + sa.toStringParsed() + " is not retrievable");
        }
        return null;
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    public void cacheGeocodedAddresses(List<GeocodedAddress> geocodedAddresses)
    {
        if (geocodedAddresses != null) {
            for (GeocodedAddress geocodedAddress : geocodedAddresses) {
                cacheGeocodedAddress(geocodedAddress);
            }
        }
    }

    private final static String SQL_INSERT_CACHE_ENTRY =
            "INSERT INTO cache.geocache (bldgnum, predir, street, streettype, postdir, location, state, zip5, latlon, method, quality, zip4) " +
                    "VALUES (:bldgnum, :predir, :street, :streettype, :postdir, :location, :state, :zip5, ST_GeomFromText( :latlon ), :method, :quality, :zip4)";

    private final static String SQL_UPDATE_CACHE_ENTRY = "UPDATE cache.geocache " +
            "SET latlon = ST_GeomFromText(:latlon), method = :method, quality = :quality, zip4 = :zip4, updated = now() " +
            "WHERE bldgnum = :bldgnum AND street = :street AND streettype = :streettype AND predir = :predir AND postdir = :postdir AND zip5 = :zip5 AND location = :location";

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
                            MapSqlParameterSource params = new MapSqlParameterSource();
                            params.addValue("latlon","POINT(" + gc.getLon() + " " + gc.getLat() + ")");
                            params.addValue("method",gc.getMethod());
                            params.addValue("quality",gc.getQuality().name());
                            params.addValue("zip4",sa.getZip4());
                            params.addValue("bldgnum",sa.getBldgNum());
                            params.addValue("street",sa.getStreetName());
                            params.addValue("streettype",sa.getStreetType());
                            params.addValue("predir",sa.getPreDir());
                            params.addValue("postdir",sa.getPostDir());
                            params.addValue("zip5",sa.getZip5());
                            params.addValue("location",sa.getLocation());

                            Integer rowUpdated = baseDao.tigerNamedJdbcTemplate.update(SQL_UPDATE_CACHE_ENTRY, params);

                            if (logger.isTraceEnabled() && rowUpdated == 1) {
                                logger.trace("Number of rows updated = " + rowUpdated);
                                logger.trace("Updated " + sa.toString() + " in cache.");
                            }

                            if (rowUpdated == 0) {
                                MapSqlParameterSource insertParams = new MapSqlParameterSource();
                                insertParams.addValue("bldgnum",Integer.valueOf(sa.getBldgNum()));
                                insertParams.addValue("predir",sa.getPreDir());
                                insertParams.addValue("street",sa.getStreetName());
                                insertParams.addValue("streettype",sa.getStreetType());
                                insertParams.addValue("postdir", sa.getPostDir());
                                insertParams.addValue("location", sa.getLocation());
                                insertParams.addValue("state", sa.getState());
                                insertParams.addValue("zip5", sa.getZip5());
                                insertParams.addValue("latlon","POINT(" + gc.getLon() + " " + gc.getLat() + ")");
                                insertParams.addValue("method", gc.getMethod());
                                insertParams.addValue("quality",gc.getQuality().name());
                                insertParams.addValue("zip4",sa.getZip4());

                                Integer rowsInserted = baseDao.tigerNamedJdbcTemplate.update(SQL_INSERT_CACHE_ENTRY, insertParams);

                                if (logger.isTraceEnabled()) {
                                    logger.trace("Number of rows inserted = " + rowsInserted);
                                    logger.trace("Inserted " + sa.toString() + " in cache.");
                                }

                            }
                            else if (rowUpdated > 1) {
                                throw new IllegalStateException("Too many updates (" + rowUpdated + ") occurred for " + sa.toString());
                            }

                        }
                        catch(Exception e) {
                            logger.error("SQL EXCEPTION", e);
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
     * Useful for debugging. Change the sql for update and insert to return an id
     */
    public class UpdateIdHandler implements ResultSetExtractor<Integer> {

        @Override
        public Integer extractData(ResultSet rs) throws SQLException {
            if (rs.next()) {
                return (Integer) rs.getInt("id");
            }
            return null;
        }
    }

    /**
     * Retrieves a GeocodedStreetAddress from the result set. This is the parsed format used for look-ups.
     * If the constructor is initialized with true, the result will be null if the geocode is not of HOUSE quality.
     * Otherwise the geocode quality won't be checked.
     */
    public class GeocodedStreetAddressHandler implements ResultSetExtractor<GeocodedStreetAddress>
    {
        private boolean buildingMatch;

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
