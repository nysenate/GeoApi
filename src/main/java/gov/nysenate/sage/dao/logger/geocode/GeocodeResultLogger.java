package gov.nysenate.sage.dao.logger.geocode;

import gov.nysenate.sage.model.api.BatchGeocodeRequest;
import gov.nysenate.sage.model.api.GeocodeRequest;
import gov.nysenate.sage.model.result.GeocodeResult;

import java.util.List;

public interface GeocodeResultLogger {

    /**
     * Logs a GeocodeRequest and the corresponding GeocodeResult to the database.
     * @param geocodeRequest
     * @param geocodeResult
     * @return int id of the logged geocode result
     */
    public int logGeocodeRequestAndResult(GeocodeRequest geocodeRequest, GeocodeResult geocodeResult);

    /**
     * Log a GeocodeResult to the database. Requires the id handle of the subsequent GeocodeRequest.
     * @param geocodeResult
     * @return int id of the logged geocode result.
     */
    public int logGeocodeResult(int geocodeRequestId, GeocodeResult geocodeResult);

    /**
     * Logs batch geocode results using a single BatchGeocodeRequest and a List of GeocodeResult objects.
     * @param batchGeoRequest
     * @param geocodeResults
     * @param flush if true, then db insert will happen right away. Otherwise it can be delayed.
     */
    public void logBatchGeocodeResults(BatchGeocodeRequest batchGeoRequest, List<GeocodeResult> geocodeResults, boolean flush);

    /**
     * Returns the current size of the batch log cache.
     * @return int
     */
    public int getLogCacheSize();

    /**
     * Logs geocode requests and results stored in the batch queue
     */
    public void flushBatchRequestsCache();
}
