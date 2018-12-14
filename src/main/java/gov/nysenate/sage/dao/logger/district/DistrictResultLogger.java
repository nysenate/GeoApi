package gov.nysenate.sage.dao.logger.district;

import gov.nysenate.sage.model.api.BatchDistrictRequest;
import gov.nysenate.sage.model.api.DistrictRequest;
import gov.nysenate.sage.model.result.DistrictResult;

import java.util.List;

public interface DistrictResultLogger {

    /**
     * Logs a DistrictRequest and the corresponding DistrictResult to the database.
     * @param districtRequest
     * @param districtResult
     * @return
     */
    public int logDistrictRequestAndResult(DistrictRequest districtRequest, DistrictResult districtResult);

    /**
     * Log a district result into the database
     * @param districtRequestId Id of the logged district request
     * @param dr DistrictResult
     * @return id of the inserted DistrictResult
     */
    public int logDistrictResult(int districtRequestId, DistrictResult dr);

    /**
     * Logs batch district results using a single BatchDistrictRequest and a List of DistrictResult objects.
     * @param batchDistRequest
     * @param districtResults
     * @param flush if true, then db insert will happen right away. Otherwise it can be delayed.
     */
    public void logBatchDistrictResults(BatchDistrictRequest batchDistRequest, List<DistrictResult> districtResults, boolean flush);

    /**
     * Returns the current size of the batch log cache.
     * @return int
     */
    public int getLogCacheSize();
}
