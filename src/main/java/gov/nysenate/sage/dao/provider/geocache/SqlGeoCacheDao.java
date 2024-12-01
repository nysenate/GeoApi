package gov.nysenate.sage.dao.provider.geocache;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.provider.nysgeo.GeocoderDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.util.TimeUtil;
import org.apache.commons.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static gov.nysenate.sage.dao.provider.geocache.SqlGeocacheQuery.*;

@Repository
public class SqlGeoCacheDao implements GeoCacheDao, GeocoderDao {
    private static final Logger logger = LoggerFactory.getLogger(SqlGeoCacheDao.class);
    private static final BlockingQueue<GeocodedAddress> cacheBuffer = new LinkedBlockingQueue<>();

    private final BaseDao baseDao;
    @Value("${geocache.buffer.size:100}")
    private int BUFFER_SIZE;

    @Autowired
    public SqlGeoCacheDao(BaseDao baseDao) {
        this.baseDao = baseDao;
    }

    /** {@inheritDoc} */
    @Override
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

    /**
     * Saves any GeocodedAddress objects stored in the buffer into the database. The address is parsed into
     * a StreetAddress object so that look-up is more reliable given variations in the address.
     */
    public synchronized void flushCacheBuffer() {
        Timestamp startTime = TimeUtil.currentTimestamp();
        int startSize = cacheBuffer.size();

        while (!cacheBuffer.isEmpty()) {
            GeocodedAddress geocodedAddress = cacheBuffer.remove();
            if (geocodedAddress == null || !geocodedAddress.isValidAddress() || !geocodedAddress.isValidGeocode()) {
                return;
            }
            Address address = geocodedAddress.getAddress();
            Geocode gc = geocodedAddress.getGeocode();
            if (isCacheable(address)) {
                var params = getIdParams(address)
                        .addValue("zip4", address.getZip4())
                        .addValue("latlon", "POINT(" + gc.lon() + " " + gc.lat() + ")")
                        .addValue("method", gc.originalGeocoder())
                        .addValue("quality", gc.quality().name());

                if (baseDao.tigerNamedJdbcTemplate.update(UPDATE_CACHE_ENTRY.getSql(), params) == 0) {
                    baseDao.tigerNamedJdbcTemplate.update(INSERT_CACHE_ENTRY.getSql(), params);
                }
            }
        }
        if (startSize > 1) {
            logger.info("Cached {} geocodes in {} ms.", startSize, TimeUtil.getElapsedMs(startTime));
        }
    }

    @Override
    public GeocodedAddress getGeocodedAddress(Address address) {
        if (isCacheable(address)) {
            // TODO: handle PO boxes elsewhere
            return baseDao.tigerNamedJdbcTemplate.query(SELECT_CACHE_ENTRY.getSql(),
                    getIdParams(address), new GeocodedStreetAddressHandler());
        }
        return null;
    }

    @Override
    public GeocodedAddress getGeocodedAddress(Point point) {
        return null;
    }

    private static class GeocodedStreetAddressHandler implements ResultSetExtractor<GeocodedAddress> {
        @Override
        public GeocodedAddress extractData(ResultSet rs) throws SQLException {
            var addr = new Address(rs.getString("bldg_id"), WordUtils.capitalizeFully(rs.getString("street")), "",
                    WordUtils.capitalizeFully(rs.getString("postal_city")), rs.getString("zip5"), rs.getString("zip4"));
            return new GeocodedAddress(addr, getGeocodeFromResultSet(rs));
        }
    }

    /**
     * Constructs a Geocode from the result set.
     * @param rs    Result set that has rs.next() already called
     */
    private static Geocode getGeocodeFromResultSet(ResultSet rs) throws SQLException {
        var point = new Point(rs.getDouble("lat"), rs.getDouble("lon"));
        GeocodeQuality quality = GeocodeQuality.fromString(rs.getString("quality"));
        return new Geocode(point, quality, rs.getString("method"), true);
    }

    private static MapSqlParameterSource getIdParams(Address address) {
        return new MapSqlParameterSource("bldgId", address.getBldgId())
                .addValue("street", address.getStreet())
                .addValue("postalCity", address.getPostalCity())
                .addValue("zip5", address.getZip5());
    }

    private static boolean isCacheable(Address addr) {
        return !addr.getStreetWithNum().isEmpty() && !addr.getPostalCity().isEmpty() && addr.getZip5() != null;
    }
}
