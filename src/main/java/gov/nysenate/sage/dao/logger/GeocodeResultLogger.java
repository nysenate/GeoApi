package gov.nysenate.sage.dao.logger;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.base.ReturnIdHandler;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.api.BatchGeocodeRequest;
import gov.nysenate.sage.model.api.GeocodeRequest;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.provider.GeoCache;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Repository
public class GeocodeResultLogger
{
    private static Logger logger = LoggerFactory.getLogger(GeocodeResultLogger.class);
    private AddressLogger addressLogger;
    private PointLogger pointLogger;
    private static GeocodeRequestLogger geocodeRequestLogger;

    private static String SCHEMA = "log";
    private static String TABLE = "geocodeResult";
    private static Boolean SAVE_LOCK = false;

    private QueryRunner run;
    private BaseDao baseDao;

    /** Batch cache */
    private static List<Pair<GeocodeRequest, GeocodeResult>> batchGeoLogCache = new ArrayList<>();

    /** Temporary cache for when the data is being saved to the database */
    private static List<Pair<GeocodeRequest, GeocodeResult>> tempCache = new ArrayList<>();

    @Autowired
    public GeocodeResultLogger(AddressLogger addressLogger, PointLogger pointLogger,
                               GeocodeRequestLogger geocodeRequestLogger, BaseDao baseDao) {
        this.addressLogger = addressLogger;
        this.pointLogger = pointLogger;
        this.geocodeRequestLogger = geocodeRequestLogger;
        this.baseDao = baseDao;
        run = this.baseDao.getQueryRunner();
    }

    /**
     * Logs a GeocodeRequest and the corresponding GeocodeResult to the database.
     * @param geocodeRequest
     * @param geocodeResult
     * @return int id of the logged geocode result
     */
    public int logGeocodeRequestAndResult(GeocodeRequest geocodeRequest, GeocodeResult geocodeResult)
    {
        int geoRequestId = geocodeRequestLogger.logGeocodeRequest(geocodeRequest);
        return logGeocodeResult(geoRequestId, geocodeResult);
    }

    /**
     * Log a GeocodeResult to the database. Requires the id handle of the subsequent GeocodeRequest.
     * @param geocodeResult
     * @return int id of the logged geocode result.
     */
    public int logGeocodeResult(int geocodeRequestId, GeocodeResult geocodeResult)
    {
        if (geocodeResult != null && geocodeRequestId > -1) {
            boolean success =  geocodeResult.isSuccess();
            boolean cacheHit = geocodeResult.getSource().equals(GeoCache.class);
            int addressId = (success) ? addressLogger.logAddress(geocodeResult.getAddress()) : 0;
            int latLonId = (success) ? pointLogger.logPoint(geocodeResult.getGeocode().getLatLon()) : 0;

            String sql =
                    "INSERT INTO " + SCHEMA + "." + TABLE + "(geocodeRequestId, success, cacheHit, addressId, method, quality, latLonId, resultTime) \n" +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?) \n" +
                    "RETURNING id";
            try {
                Geocode geocode = geocodeResult.getGeocode();
                String method = (success) ? geocode.getMethod() : null;
                String quality = (success) ? geocode.getQuality().name() : null;
                return run.query(sql, new ReturnIdHandler(), (geocodeRequestId > 0) ? geocodeRequestId : null, success, cacheHit,
                                                             (addressId > 0) ? addressId : null, method, quality, (latLonId > 0) ? latLonId : null,
                                                             geocodeResult.getResultTime());
            }
            catch (SQLException ex) {
                logger.error("Failed to log geocode result!", ex);
            }
        }
        return 0;
    }

    /**
     * Logs batch geocode results using a single BatchGeocodeRequest and a List of GeocodeResult objects.
     * @param batchGeoRequest
     * @param geocodeResults
     * @param flush if true, then db insert will happen right away. Otherwise it can be delayed.
     */
    public void logBatchGeocodeResults(BatchGeocodeRequest batchGeoRequest, List<GeocodeResult> geocodeResults, boolean flush)
    {
        if (batchGeoRequest != null && geocodeResults != null) {
            try {
                if (batchGeoRequest.getAddresses().size() > 0) {
                    for (int i = 0; i < batchGeoRequest.getAddresses().size(); i++) {
                        Address address = batchGeoRequest.getAddresses().get(i);
                        GeocodeRequest geoRequest = (GeocodeRequest) batchGeoRequest.clone();
                        geoRequest.setAddress(address);
                        if (!SAVE_LOCK) {
                            batchGeoLogCache.add(new ImmutablePair<>(geoRequest, geocodeResults.get(i)));
                        }
                        else {
                            logger.debug("Logging geocode request to temporary list.");
                            tempCache.add(new ImmutablePair<>(geoRequest, geocodeResults.get(i)));
                        }
                    }
                }
                else if (batchGeoRequest.getPoints().size() > 0 && batchGeoRequest.isReverse()) {
                    for (int i = 0; i < batchGeoRequest.getPoints().size(); i++) {
                        Point point = batchGeoRequest.getPoints().get(i);
                        GeocodeRequest geoRequest = (GeocodeRequest) batchGeoRequest.clone();
                        geoRequest.setPoint(point);
                        if (!SAVE_LOCK) {
                            batchGeoLogCache.add(new ImmutablePair<>(geoRequest, geocodeResults.get(i)));
                        }
                        else {
                            logger.debug("Logging revgeocode result to temporary list.");
                            tempCache.add(new ImmutablePair<>(geoRequest, geocodeResults.get(i)));
                        }
                    }
                }
            }
            catch (Exception ex) {
                logger.error("Failed to log batch geocode results!", ex);
            }

            if (flush) {
                flushBatchRequestsCache();
            }
        }
    }

    /**
     * Returns the current size of the batch log cache.
     * @return int
     */
    public int getLogCacheSize()
    {
        return batchGeoLogCache.size();
    }

    /**
     * Logs geocode requests and results stored in the batch queue
     */
    public synchronized void flushBatchRequestsCache()
    {
        logger.debug("Flushing geocode batch log! Locking the batch log cache.");
        SAVE_LOCK = true;
        try {
            Iterator<Pair<GeocodeRequest, GeocodeResult>> geoPairIterator = batchGeoLogCache.iterator();
            while (geoPairIterator.hasNext()) {
                try {
                    Pair<GeocodeRequest, GeocodeResult> geoPair = geoPairIterator.next();
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
    private synchronized void moveTempToMainCache()
    {
        logger.debug("Transferring temp geocode pairs to main batch..");
        batchGeoLogCache.addAll(new ArrayList<>(tempCache));
        tempCache.clear();
        logger.debug("Main batch size: " + batchGeoLogCache.size());
    }
}
