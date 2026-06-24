package gov.nysenate.sage.provider.geocache;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.provider.nysgeo.GeocoderDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.Accuracy;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.provider.geocode.Geocoder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static gov.nysenate.sage.provider.geocache.SqlGeocacheQuery.*;

@Service
public class GeoCache extends BaseDao implements GeocoderDao {
    @Override
    public Geocoder geocoder() {
        return Geocoder.GEOCACHE;
    }

    @Override
    public GeocodedAddress getGeocodedAddress(Address address) {
        if (address.isCacheable()) {
            List<GeocodedAddress> geoAddrs = namedJdbcTemplate.query(SELECT_CACHE_ENTRY.getSql(),
                    getIdParams(address), new GeocodedStreetAddressMapper());
            if (!geoAddrs.isEmpty()) {
                return geoAddrs.getFirst();
            }
        }
        return null;
    }

    public void cache(GeocodedAddress geoAddr) {
        if (geoAddr == null || geoAddr.getAddress() == null ||
                !geoAddr.getAddress().isCacheable() || !geoAddr.isValidGeocode()) {
            return;
        }
        Geocode gc = geoAddr.getGeocode();
        var params = getIdParams(geoAddr.getAddress())
                .addValue("latlon", "POINT(" + gc.lon() + " " + gc.lat() + ")")
                .addValue("method", gc.originalGeocoder().name())
                .addValue("accuracy", gc.accuracy().name());

        synchronized (this) {
            if (namedJdbcTemplate.update(UPDATE_CACHE_ENTRY.getSql(), params) == 0) {
                namedJdbcTemplate.update(INSERT_CACHE_ENTRY.getSql(), params);
            }
        }
    }

    private static class GeocodedStreetAddressMapper implements RowMapper<GeocodedAddress> {
        @Override
        public GeocodedAddress mapRow(ResultSet rs, int rowNum) throws SQLException {
            var addr = new Address(rs.getString("primary_addr1"),
                    WordUtils.capitalizeFully(rs.getString("postal_city")),
                    rs.getString("state"), rs.getString("zip5"));
            var point = new Point(rs.getString("lat"), rs.getString("lon"));
            Accuracy accuracy = Accuracy.fromString(rs.getString("accuracy"));
            Geocoder geocoder = Geocoder.valueOf(rs.getString("method"));
            return new GeocodedAddress(addr, new Geocode(point, accuracy, geocoder, true));
        }
    }

    private static MapSqlParameterSource getIdParams(Address address) {
        return new MapSqlParameterSource("primaryAddr1", StringUtils.upperCase(address.getPrimaryAddr1()))
                .addValue("postalCity", StringUtils.upperCase(address.getPostalCity()))
                .addValue("state", StringUtils.upperCase(address.getState()))
                .addValue("zip5", address.getZip5().toString());
    }
}
