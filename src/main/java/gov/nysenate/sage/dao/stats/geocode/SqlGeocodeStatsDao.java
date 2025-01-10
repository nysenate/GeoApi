package gov.nysenate.sage.dao.stats.geocode;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.stats.GeocodeStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

/**
 * Retrieves stats pertaining to geocoder usage.
 */
@Repository
public class SqlGeocodeStatsDao extends BaseDao {
    private static final Logger logger = LoggerFactory.getLogger(SqlGeocodeStatsDao.class);

    /**
     * Retrieve geocode stats within a specified time frame.
     * @return GeocodeStats
     */
    public GeocodeStats getGeocodeStats(Timestamp from, Timestamp to) {
        try {
            var params = new MapSqlParameterSource("from", from).addValue("to", to);
            List<GeocodeStats> gsList = geoApiNamedJbdcTemplate.query(
                    GeocodeStatsQuery.GET_TOTAL_COUNT.getSql(getLogSchema()), params, new TotalCountsHandler());
            if (gsList.get(0) != null) {
                GeocodeStats gs =  gsList.get(0);
                List<GeocodeStats> geocodeStats =
                        geoApiNamedJbdcTemplate.query(
                                GeocodeStatsQuery.GET_GEOCODER_USAGE.getSql(getLogSchema()),
                                params, new GeocoderUsageHandler(gs));
                return geocodeStats.get(0);
            }
        }
        catch (Exception ex) {
            logger.error("Failed to get geocode stats!", ex);
        }
        return null;
    }

    /** Handler for result set of totalCountsSql */
    private static class TotalCountsHandler implements RowMapper<GeocodeStats> {
        @Override
        public GeocodeStats mapRow(ResultSet rs, int rowNum) throws SQLException {
            var gs = new GeocodeStats();
            if (rs.next()) {
                gs.setTotalGeocodes(rs.getInt("totalGeocodes"));
                gs.setTotalRequests(rs.getInt("totalRequests"));
                gs.setTotalCacheHits(rs.getInt("cacheHits"));
                return gs;
            }
            return null;
        }
    }

    private record GeocoderUsageHandler(GeocodeStats gs) implements RowMapper<GeocodeStats> {
        @Override
        public GeocodeStats mapRow(ResultSet rs, int rowNum) throws SQLException {
            while (rs.next()) {
                gs.addGeocoderUsage(rs.getString("method"), rs.getInt("requests"));
            }
            return gs;
        }
    }
}
