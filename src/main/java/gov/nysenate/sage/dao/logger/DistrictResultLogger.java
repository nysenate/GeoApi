package gov.nysenate.sage.dao.logger;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.base.ReturnIdHandler;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.api.BatchDistrictRequest;
import gov.nysenate.sage.model.api.DistrictRequest;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.DistrictResult;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DistrictResultLogger extends BaseDao
{
    private static Logger logger = Logger.getLogger(DistrictResultLogger.class);
    private static DistrictRequestLogger distRequestLogger = new DistrictRequestLogger();

    private static String SCHEMA = "log";
    private static String TABLE = "districtResult";
    private static Boolean SAVE_LOCK = false;

    private QueryRunner run = getQueryRunner();

    /** Batch cache */
    private static List<Pair<DistrictRequest, DistrictResult>> batchDistLogCache = new ArrayList<>();

    /** Temporary cache for when the data is being saved to the database */
    private static List<Pair<DistrictRequest, DistrictResult>> tempCache = new ArrayList<>();

    /**
     * Logs a DistrictRequest and the corresponding DistrictResult to the database.
     * @param districtRequest
     * @param districtResult
     * @return
     */
    public int logDistrictRequestAndResult(DistrictRequest districtRequest, DistrictResult districtResult)
    {
        int distRequestId = distRequestLogger.logDistrictRequest(districtRequest);
        return logDistrictResult(distRequestId, districtResult);
    }

    /**
     * Log a district result into the database
     * @param districtRequestId Id of the logged district request
     * @param dr DistrictResult
     * @return id of the inserted DistrictResult
     */
    public int logDistrictResult(int districtRequestId, DistrictResult dr)
    {
        logger.trace("Starting to log district result");
        if (dr != null) {
            String sd = null, ad = null, cd = null, cc = null, town = null, school = null;
            if (dr.isSuccess()) {
                DistrictInfo dinfo = dr.getDistrictedAddress().getDistrictInfo();
                sd = dinfo.getDistCode(DistrictType.SENATE);
                ad = dinfo.getDistCode(DistrictType.ASSEMBLY);
                cd = dinfo.getDistCode(DistrictType.CONGRESSIONAL);
                cc = dinfo.getDistCode(DistrictType.COUNTY);
                town = dinfo.getDistCode(DistrictType.TOWN);
                school = dinfo.getDistCode(DistrictType.SCHOOL);
            }
            String sql = "INSERT INTO " + SCHEMA + "." + TABLE + "(districtrequestid, assigned, status, senatecode, assemblycode," +
                                                                  "congressionalcode, countycode, town_code, school_code, matchLevel, resulttime) \n" +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) \n" +
                         "RETURNING id";
            try {
                int id = run.query(sql, new ReturnIdHandler(), (districtRequestId > 0) ? districtRequestId : null, dr.isSuccess(), dr.getStatusCode().name(),
                                                             sd, ad, cd, cc, town, school, dr.getDistrictMatchLevel().name(), dr.getResultTime());
                logger.trace("Saved district result id: " + id);
                return id;
            }
            catch (SQLException ex) {
                logger.error("Failed to log district result!", ex);
            }
        }
        else {
            logger.error("DistrictResult was null, cannot be logged!");
        }
        return 0;
    }

    /**
     * Logs batch district results using a single BatchDistrictRequest and a List of DistrictResult objects.
     * @param batchDistRequest
     * @param districtResults
     * @param flush if true, then db insert will happen right away. Otherwise it can be delayed.
     */
    public void logBatchDistrictResults(BatchDistrictRequest batchDistRequest, List<DistrictResult> districtResults, boolean flush) {
        if (batchDistRequest != null && districtResults != null) {
            for (int i = 0; i < batchDistRequest.getGeocodedAddresses().size(); i++) {
                try {
                    GeocodedAddress geocodedAddress = batchDistRequest.getGeocodedAddresses().get(i);
                    DistrictRequest districtRequest = (DistrictRequest) batchDistRequest.clone();
                    districtRequest.setGeocodedAddress(geocodedAddress);
                    if (!SAVE_LOCK) {
                        batchDistLogCache.add(new ImmutablePair<>(districtRequest, districtResults.get(i)));
                    }
                    else {
                        logger.debug("Logging district result to temporary list.");
                        tempCache.add(new ImmutablePair<>(districtRequest, districtResults.get(i)));
                    }
                }
                catch (Exception ex) {
                    logger.error("Failed to log batch district results!", ex);
                }
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
        return batchDistLogCache.size();
    }

    /**
     * Logs district requests and results stored in the batch queue
     */
    public synchronized void flushBatchRequestsCache()
    {
        logger.debug("Flushing district batch log of size " + batchDistLogCache.size());
        SAVE_LOCK = true;
        try {
            Iterator<Pair<DistrictRequest, DistrictResult>> distPairIterator = batchDistLogCache.iterator();
            while (distPairIterator.hasNext()) {
                try {
                    Pair<DistrictRequest, DistrictResult> distPair = distPairIterator.next();
                    if (distPair != null) {
                        logDistrictRequestAndResult(distPair.getLeft(), distPair.getRight());
                    }
                }
                catch (Exception ex) {
                    logger.warn("Failed to flush district request/result pair to log: ", ex);
                }
            }
        }
        catch (Exception ex) {
            logger.error("Failed to flush district batch. Clearing now. ", ex);
        }
        batchDistLogCache.clear();
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
        batchDistLogCache.addAll(new ArrayList<>(tempCache));
        tempCache.clear();
        logger.debug("Main batch size: " + batchDistLogCache.size());
    }
}