package gov.nysenate.sage.provider.geocache;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.provider.nysgeo.GeocoderDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.BuildingAddress;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.provider.geocode.Geocoder;
import org.apache.commons.text.WordUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static gov.nysenate.sage.dao.provider.geocache.SqlGeocacheQuery.*;

@Service
public class GeoCache extends BaseDao implements GeocoderDao {
    private final BlockingQueue<GeocodedAddress> cacheBuffer = new LinkedBlockingQueue<>();

    @Override
    public Geocoder geocoder() {
        return Geocoder.GEOCACHE;
    }

    @Override
    public GeocodedAddress getGeocodedAddress(BuildingAddress address) {
        if (address.isValid()) {
            List<GeocodedAddress> geoAddrs = namedJdbcTemplate.query(SELECT_CACHE_ENTRY.getSql(),
                    getIdParams(address), new GeocodedStreetAddressMapper());
            if (!geoAddrs.isEmpty()) {
                return geoAddrs.get(0);
            }
        }
        return null;
    }

    public void cache(GeocodeResult geocodeResult) {
        internalCache(geocodeResult);
        flushCacheBuffer();
    }

    public void cache(List<GeocodeResult> geocodeResults) {
        for (GeocodeResult result : geocodeResults) {
            internalCache(result);
        }
        flushCacheBuffer();
    }

    private void internalCache(GeocodeResult result) {
        if (result == null || !result.isSuccess() || result.getSource() == Geocoder.GEOCACHE) {
            return;
        }
        GeocodedAddress geoAddr = result.getGeocodedAddress();
        if (geoAddr != null && geoAddr.isValidAddress() && geoAddr.isValidGeocode() &&
                !geoAddr.getGeocode().isCached() && !geoAddr.getAddress().isPoBox()) {
            cacheBuffer.add(geoAddr);
        }
    }

    private static class GeocodedStreetAddressMapper implements RowMapper<GeocodedAddress> {
        @Override
        public GeocodedAddress mapRow(ResultSet rs, int rowNum) throws SQLException {
            var addr = new BuildingAddress(rs.getString("bldg_id"), WordUtils.capitalizeFully(rs.getString("street")),
                    WordUtils.capitalizeFully(rs.getString("postal_city")), "NY", rs.getString("zip5"), rs.getString("zip4"));
            return new GeocodedAddress(addr, getGeocodeFromResultSet(rs));
        }
    }

    /**
     * Constructs a Geocode from the result set.
     * @param rs Result set that has rs.next() already called
     */
    private static Geocode getGeocodeFromResultSet(ResultSet rs) throws SQLException {
        var point = new Point(rs.getString("lat"), rs.getString("lon"));
        GeocodeQuality quality = GeocodeQuality.fromString(rs.getString("quality"));
        return new Geocode(point, quality, rs.getString("method"), true);
    }

    // TODO: only use zip4 if necessary
    private static MapSqlParameterSource getIdParams(BuildingAddress address) {
        return new MapSqlParameterSource("bldgId", address.getBldgId().toUpperCase())
                .addValue("street", address.getStreet().toUpperCase())
                .addValue("postalCity", address.getPostalCity().toUpperCase())
                .addValue("zip5", address.getZip5().toString().toUpperCase());
    }

    /**
     * Saves any GeocodedAddress objects stored in the buffer into the database.
     */
    private synchronized void flushCacheBuffer() {
        while (!cacheBuffer.isEmpty()) {
            GeocodedAddress geocodedAddress = cacheBuffer.remove();
            Address address = geocodedAddress.getAddress();
            Geocode gc = geocodedAddress.getGeocode();
            var params = getIdParams(((BuildingAddress) address))
                    .addValue("zip4", address.getZip4().toString())
                    .addValue("latlon", "POINT(" + gc.lon() + " " + gc.lat() + ")")
                    .addValue("method", gc.originalGeocoder().name())
                    .addValue("quality", gc.quality().name());

            if (namedJdbcTemplate.update(UPDATE_CACHE_ENTRY.getSql(), params) == 0) {
                namedJdbcTemplate.update(INSERT_CACHE_ENTRY.getSql(), params);
            }
        }
    }
}
