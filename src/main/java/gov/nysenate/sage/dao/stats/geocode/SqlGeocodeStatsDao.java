package gov.nysenate.sage.dao.stats.geocode;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.stats.GeocodeStats;
import gov.nysenate.sage.provider.geocode.Geocoder;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Retrieves stats pertaining to geocoder usage.
 */
@Repository
public class SqlGeocodeStatsDao extends BaseDao {
    public void putGeocodedAddress(Geocoder currGeocoder, GeocodedAddress result) {
        var params = new MapSqlParameterSource("geocoder", currGeocoder.toString())
                .addValue("success", result != null && result.getGeocode().isValidGeocode());
        namedJdbcTemplate.update(GeocodeStatsQuery.INSERT_GEOCODE_STATS.getSql(getLogSchema()), params);
    }

    /**
     * Retrieve geocode stats within a specified time frame.
     * @return GeocodeStats
     */
    public GeocodeStats getGeocodeStats(Timestamp from, Timestamp to) {
        var params = new MapSqlParameterSource("from", from).addValue("to", to);
        return namedJdbcTemplate.query(GeocodeStatsQuery.GET_TOTAL_STATS.getSql(getLogSchema()), params, new TotalCountsHandler());
    }

    /** Handler for result set of totalCountsSql */
    private static class TotalCountsHandler implements ResultSetExtractor<GeocodeStats> {
        @Override
        public GeocodeStats extractData(ResultSet rs) throws SQLException, DataAccessException {
            var gs = new GeocodeStats();
            if (rs.next()) {
                Geocoder geocoder = Geocoder.valueOf(rs.getString("geocoder"));
                gs.addGeocoderUsage(geocoder, rs.getBoolean("success"), rs.getInt("count"));
            }
            return gs;
        }
    }
}
