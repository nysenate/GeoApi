package gov.nysenate.sage.dao.logger.geocode;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.logger.address.SqlAddressLogger;
import gov.nysenate.sage.dao.logger.point.SqlPointLogger;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.api.BatchGeocodeRequest;
import gov.nysenate.sage.model.api.SingleGeocodeRequest;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.provider.geocode.Geocoder;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Repository
public class SqlGeocodeResultLogger implements GeocodeResultLogger {
    private static final Logger logger = LoggerFactory.getLogger(SqlGeocodeResultLogger.class);
    private static boolean SAVE_LOCK = false;
    private final SqlAddressLogger sqlAddressLogger;
    private final SqlPointLogger sqlPointLogger;
    private final SqlGeocodeRequestLogger sqlGeocodeRequestLogger;
    private final BaseDao baseDao;

    /** Batch cache */
    private final List<Pair<SingleGeocodeRequest, GeocodeResult>> batchGeoLogCache = new ArrayList<>();

    /** Temporary cache for when the data is being saved to the database */
    private final List<Pair<SingleGeocodeRequest, GeocodeResult>> tempCache = new ArrayList<>();

    @Autowired
    public SqlGeocodeResultLogger(SqlAddressLogger sqlAddressLogger, SqlPointLogger sqlPointLogger,
                                  SqlGeocodeRequestLogger sqlGeocodeRequestLogger, BaseDao baseDao) {
        this.sqlAddressLogger = sqlAddressLogger;
        this.sqlPointLogger = sqlPointLogger;
        this.sqlGeocodeRequestLogger = sqlGeocodeRequestLogger;
        this.baseDao = baseDao;
    }

    /**
     * Logs a GeocodeRequest and the corresponding GeocodeResult to the database.
     * @return int id of the logged geocode result
     */
    public int logGeocodeRequestAndResult(SingleGeocodeRequest geocodeRequest, GeocodeResult geocodeResult) {
        int geoRequestId = sqlGeocodeRequestLogger.logGeocodeRequest(geocodeRequest);
        return logGeocodeResult(geoRequestId, geocodeResult);
    }

    /**
     * Log a GeocodeResult to the database. Requires the id handle of the subsequent GeocodeRequest.
     * @return int id of the logged geocode result.
     */
    public int logGeocodeResult(int geocodeRequestId, GeocodeResult geocodeResult) {
        if (geocodeResult == null || geocodeRequestId <= -1) {
            return 0;
        }
        boolean success = geocodeResult.isSuccess();
        boolean cacheHit = geocodeResult.getSource().equals(Geocoder.GEOCACHE);
        int addressId = (success) ? sqlAddressLogger.logAddress(geocodeResult.getAddress()) : 0;
        int latLonId = (success) ? sqlPointLogger.logPoint(geocodeResult.getGeocode().point()) : 0;

        try {
            Geocode geocode = geocodeResult.getGeocode();
            String method = (success) ? geocode.originalGeocoder().name() : null;
            String quality = (success) ? geocode.quality().name() : null;

            var params = new MapSqlParameterSource()
                    .addValue("geocodeRequestId", (geocodeRequestId > 0) ? geocodeRequestId : null)
                    .addValue("success", success)
                    .addValue("cacheHit", cacheHit)
                    .addValue("addressId", (addressId > 0) ? addressId : null)
                    .addValue("method", method)
                    .addValue("quality", quality)
                    .addValue("latLonId", (latLonId > 0) ? latLonId : null)
                    .addValue("resultTime", geocodeResult.getResultTime());

            List<Integer> idList = baseDao.geoApiNamedJbdcTemplate.query(
                    GeocodeResultQuery.INSERT_RESULT.getSql(baseDao.getLogSchema()),
                    params, new GeocodeResultIdHandler());

            return idList.get(0);

        }
        catch (Exception ex) {
            logger.error("Failed to log geocode result!", ex);
        }
        return 0;
    }

    /**
     * Logs batch geocode results using a single BatchGeocodeRequest and a List of GeocodeResult objects.
     * @param flush if true, then db insert will happen right away. Otherwise, it can be delayed.
     */
    public void logBatchGeocodeResults(BatchGeocodeRequest batchGeoRequest, List<GeocodeResult> geocodeResults, boolean flush) {
        if (batchGeoRequest == null || geocodeResults == null) {
            return;
        }
        for (int i = 0; i < batchGeoRequest.getAddresses().size(); i++) {
            Address address = batchGeoRequest.getAddresses().get(i);
            var geoRequest = new SingleGeocodeRequest(batchGeoRequest, address);
            if (!SAVE_LOCK) {
                batchGeoLogCache.add(new ImmutablePair<>(geoRequest, geocodeResults.get(i)));
            }
            else {
                tempCache.add(new ImmutablePair<>(geoRequest, geocodeResults.get(i)));
            }
        }
        if (batchGeoRequest.isReverse()) {
            for (int i = 0; i < batchGeoRequest.getPoints().size(); i++) {
                Point point = batchGeoRequest.getPoints().get(i);
                var geoRequest = new SingleGeocodeRequest(batchGeoRequest, point);
                geoRequest.setPoint(point);
                if (!SAVE_LOCK) {
                    batchGeoLogCache.add(new ImmutablePair<>(geoRequest, geocodeResults.get(i)));
                }
                else {
                    tempCache.add(new ImmutablePair<>(geoRequest, geocodeResults.get(i)));
                }
            }
        }

        if (flush) {
            flushBatchRequestsCache();
        }
    }

    /**
     * Returns the current size of the batch log cache.
     * @return int
     */
    public int getLogCacheSize() {
        return batchGeoLogCache.size();
    }

    /**
     * Logs geocode requests and results stored in the batch queue
     */
    public synchronized void flushBatchRequestsCache() {
        logger.debug("Flushing geocode batch log! Locking the batch log cache.");
        SAVE_LOCK = true;
        try {
            Iterator<Pair<SingleGeocodeRequest, GeocodeResult>> geoPairIterator = batchGeoLogCache.iterator();
            while (geoPairIterator.hasNext()) {
                try {
                    Pair<SingleGeocodeRequest, GeocodeResult> geoPair = geoPairIterator.next();
                    if (geoPair != null) {
                        logGeocodeRequestAndResult(geoPair.getLeft(), geoPair.getRight());
                    }
                }
                catch (Exception ex) {
                    logger.warn("Failed to flush geocode request/result pair into log: ", ex);
                }
            }
        }
        catch (Exception ex) {
            logger.error("Failed to flush geocode batch. Clearing now. ", ex);
        }
        batchGeoLogCache.clear();
        moveTempToMainCache();
        SAVE_LOCK = false;
    }

    /**
     * While the request/response pairs are being saved, any new pairs that are being logged are appended
     * to a temporary list such that the main batch list does not encounter concurrent modification issues.
     * Once the batch list has been flushed this method is called to transfer all the pairs stored in the
     * temp list back to the main batch list.
     */
    private synchronized void moveTempToMainCache() {
        batchGeoLogCache.addAll(new ArrayList<>(tempCache));
        tempCache.clear();
    }


    private static class GeocodeResultIdHandler implements RowMapper<Integer> {
        @Override
        public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getInt("id");
        }
    }
}
