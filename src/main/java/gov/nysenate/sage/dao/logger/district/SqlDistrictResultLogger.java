package gov.nysenate.sage.dao.logger.district;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.api.BatchDistrictRequest;
import gov.nysenate.sage.model.api.DistrictRequest;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.DistrictResult;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Repository
public class SqlDistrictResultLogger implements DistrictResultLogger
{
    private static Logger logger = LoggerFactory.getLogger(SqlDistrictResultLogger.class);
    private static SqlDistrictRequestLogger distRequestLogger;
    private static Boolean SAVE_LOCK = false;
    private BaseDao baseDao;

    @Autowired
    public SqlDistrictResultLogger(SqlDistrictRequestLogger distRequestLogger, BaseDao baseDao) {
        this.distRequestLogger = distRequestLogger;
        this.baseDao = baseDao;
    }

    /** Batch cache */
    private static List<Pair<DistrictRequest, DistrictResult>> batchDistLogCache = new ArrayList<>();

    /** Temporary cache for when the data is being saved to the database */
    private static List<Pair<DistrictRequest, DistrictResult>> tempCache = new ArrayList<>();

    /** {@inheritDoc} */
    public int logDistrictRequestAndResult(DistrictRequest districtRequest, DistrictResult districtResult)
    {
        int distRequestId = distRequestLogger.logDistrictRequest(districtRequest);
        return logDistrictResult(distRequestId, districtResult);
    }

    /** {@inheritDoc} */
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
            try {

                MapSqlParameterSource params = new MapSqlParameterSource();
                params.addValue("districtrequestid",(districtRequestId > 0) ? districtRequestId : null);
                params.addValue("assigned",dr.isSuccess());
                params.addValue("status",dr.getStatusCode().name());
                params.addValue("senatecode",sd);
                params.addValue("assemblycode",ad);
                params.addValue("congressionalcode",cd);
                params.addValue("countycode",cc);
                params.addValue("town_code",town);
                params.addValue("school_code",school);
                params.addValue("matchLevel",dr.getDistrictMatchLevel().name());
                params.addValue("resulttime",dr.getResultTime());

                List<Integer> idList = baseDao.geoApiNamedJbdcTemaplate.query(
                        DistrictResultQuery.INSERT_RESULT.getSql(baseDao.getLogSchema()),
                        params, new DistrictRequestIdHandler());

                int id = idList.get(0);
                logger.trace("Saved district result id: " + id);
                return id;
            }
            catch (Exception ex) {
                logger.error("Failed to log district result!", ex);
            }
        }
        else {
            logger.error("DistrictResult was null, cannot be logged!");
        }
        return 0;
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
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

    private static class DistrictRequestIdHandler implements RowMapper<Integer> {
        @Override
        public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getInt("id");
        }
    }
}