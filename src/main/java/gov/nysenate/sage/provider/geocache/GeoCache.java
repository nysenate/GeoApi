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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static gov.nysenate.sage.dao.provider.geocache.SqlGeocacheQuery.*;

@Service
public class GeoCache extends BaseDao implements GeocoderDao {
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

    private static class GeocodedStreetAddressMapper implements RowMapper<GeocodedAddress> {
        @Override
        public GeocodedAddress mapRow(ResultSet rs, int rowNum) throws SQLException {
            var addr = new BuildingAddress(rs.getString("bldg_id"), WordUtils.capitalizeFully(rs.getString("street")),
                    WordUtils.capitalizeFully(rs.getString("postal_city")), rs.getString("state"),
                    rs.getString("zip5"), rs.getString("zip4"));
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

    private static MapSqlParameterSource getIdParams(BuildingAddress address) {
        return new MapSqlParameterSource("bldgId", StringUtils.upperCase(address.getBldgId()))
                .addValue("street", StringUtils.upperCase(address.getStreet()))
                .addValue("postalCity", StringUtils.upperCase(address.getPostalCity()))
                .addValue("state", StringUtils.upperCase(address.getState()))
                .addValue("zip5", address.getZip5().toString())
                .addValue("zip4", address.getZip4() == null ? null : address.getZip4().toString());
    }

    public void cache(GeocodeResult result) {
        cache(List.of(result));
    }

    /**
     * Saves any GeocodedAddress objects stored in the buffer into the database.
     */
    public synchronized void cache(List<GeocodeResult> geocodeResults) {
        for (GeocodeResult result : geocodeResults) {
            if (result == null || !result.isSuccess() || result.getSource() == Geocoder.GEOCACHE) {
                continue;
            }
            GeocodedAddress geoAddr = result.getGeocodedAddress();
            if (geoAddr == null || !geoAddr.isValidAddress() || !geoAddr.isValidGeocode() ||
                    geoAddr.getGeocode().isCached() || geoAddr.getAddress().isPoBox()) {
                continue;
            }
            Address address = geoAddr.getAddress();
            Geocode gc = geoAddr.getGeocode();
            var params = getIdParams(((BuildingAddress) address))
                    .addValue("latlon", "POINT(" + gc.lon() + " " + gc.lat() + ")")
                    .addValue("method", gc.originalGeocoder().name())
                    .addValue("quality", gc.quality().name());

            if (namedJdbcTemplate.update(UPDATE_CACHE_ENTRY.getSql(), params) == 0) {
                namedJdbcTemplate.update(INSERT_CACHE_ENTRY.getSql(), params);
            }
        }
    }
}
