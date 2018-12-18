package gov.nysenate.sage.dao.stats.geocode;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.stats.deployment.SqlDeploymentStatsDao;
import gov.nysenate.sage.model.stats.GeocodeStats;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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
public class SqlGeocodeStatsDao implements GeocodeStatsDao
{
    private static Logger logger = LoggerFactory.getLogger(SqlGeocodeStatsDao.class);
    private SqlDeploymentStatsDao sqlDeploymentStatsDao;
    private BaseDao baseDao;
    private QueryRunner run;

    @Autowired
    public SqlGeocodeStatsDao(BaseDao baseDao, SqlDeploymentStatsDao sqlDeploymentStatsDao) {
        this.baseDao = baseDao;
        this.sqlDeploymentStatsDao = sqlDeploymentStatsDao;
        run = this.baseDao.getQueryRunner();
    }

    /**
     * Retrieve geocode stats within a specified time frame.
     * @return GeocodeStats
     */
    public GeocodeStats getGeocodeStats(Timestamp from, Timestamp to)
    {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("from", from);
            params.addValue("to", to);

            List<GeocodeStats> gsList = baseDao.geoApiNamedJbdcTemaplate.query(
                    GeocodeStatsQuery.GET_TOTAL_COUNT.getSql(baseDao.getLogSchema()), params, new TotalCountsHandler());
            if (gsList != null && gsList.get(0) != null) {
                GeocodeStats gs =  gsList.get(0);
                List<GeocodeStats> geocodeStats =
                        baseDao.geoApiNamedJbdcTemaplate.query(
                                GeocodeStatsQuery.GET_GEOCODER_USAGE.getSql(baseDao.getLogSchema()),
                                params ,new GeocoderUsageHandler(gs));
                return geocodeStats.get(0);
            }
        }
        catch (Exception ex) {
            logger.error("Failed to get geocode stats!", ex);
        }
        return null;
    }

    /** Handler for result set of totalCountsSql */
    class TotalCountsHandler implements RowMapper<GeocodeStats> {
        public GeocodeStats mapRow(ResultSet rs, int rowNum) throws SQLException {
            GeocodeStats gs = new GeocodeStats();
            if (rs.next()) {
                gs.setTotalGeocodes(rs.getInt("totalGeocodes"));
                gs.setTotalRequests(rs.getInt("totalRequests"));
                gs.setTotalCacheHits(rs.getInt("cacheHits"));
                return gs;
            }
            return null;
        }
    }

    /** Handler for result set of geocoderUsageSql */
    class GeocoderUsageHandler implements RowMapper<GeocodeStats> {
        GeocodeStats gs;
        public GeocoderUsageHandler(GeocodeStats gs) {
            this.gs = gs;
        }

        public GeocodeStats mapRow(ResultSet rs, int rowNum) throws SQLException {
            while (rs.next()) {
                gs.addGeocoderUsage(rs.getString("method"), rs.getInt("requests"));
            }
            return gs;
        }
    }
}