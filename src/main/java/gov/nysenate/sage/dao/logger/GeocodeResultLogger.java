package gov.nysenate.sage.dao.logger;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.base.ReturnIdHandler;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.api.BatchGeocodeRequest;
import gov.nysenate.sage.model.api.DistrictRequest;
import gov.nysenate.sage.model.api.GeocodeRequest;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.provider.GeoCache;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeocodeResultLogger extends BaseDao
{
    private static Logger logger = Logger.getLogger(GeocodeResultLogger.class);
    private static AddressLogger addressLogger = new AddressLogger();
    private static PointLogger pointLogger = new PointLogger();
    private static GeocodeRequestLogger geocodeRequestLogger = new GeocodeRequestLogger();

    private static String SCHEMA = "log";
    private static String TABLE = "geocodeResult";
    private QueryRunner run = getQueryRunner();

    /** Batch cache */
    private static List<Pair<GeocodeRequest, GeocodeResult>> batchGeoLogCache = new ArrayList<>();

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
            boolean cacheHit = geocodeResult.getSource().equalsIgnoreCase(GeoCache.class.getSimpleName());
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
                        batchGeoLogCache.add(new ImmutablePair<>(geoRequest, geocodeResults.get(i)));
                    }
                }
                else if (batchGeoRequest.getPoints().size() > 0 && batchGeoRequest.isReverse()) {
                    for (int i = 0; i < batchGeoRequest.getPoints().size(); i++) {
                        Point point = batchGeoRequest.getPoints().get(i);
                        GeocodeRequest geoRequest = (GeocodeRequest) batchGeoRequest.clone();
                        geoRequest.setPoint(point);
                        batchGeoLogCache.add(new ImmutablePair<>(geoRequest, geocodeResults.get(i)));
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
     * Logs geocode requests and results stored in the batch queue
     */
    public void flushBatchRequestsCache()
    {
        logger.debug("Flushing geocode batch log");
        for (Pair<GeocodeRequest, GeocodeResult> geoPair : batchGeoLogCache) {
            logGeocodeRequestAndResult(geoPair.getLeft(), geoPair.getRight());
        }
        batchGeoLogCache.clear();
    }
}
