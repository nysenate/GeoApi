package gov.nysenate.sage.dao.logger;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.base.ReturnIdHandler;
import gov.nysenate.sage.model.api.ApiRequest;
import gov.nysenate.sage.model.api.DistrictRequest;
import gov.nysenate.sage.model.job.JobProcess;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

@Repository
public class DistrictRequestLogger extends BaseDao
{
    private static Logger logger = LogManager.getLogger(DistrictRequestLogger.class);
    private static AddressLogger addressLogger = new AddressLogger();
    private static String SCHEMA = "log";
    private static String TABLE = "districtRequest";
    private QueryRunner run = getQueryRunner();

    /**
     * Log a DistrictRequest to the database
     * @param dr DistrictRequest
     * @return id of district request. This id is set to the supplied districtRequest as well.
     */
    public int logDistrictRequest(DistrictRequest dr)
    {
        if (dr != null) {
            ApiRequest apiRequest = dr.getApiRequest();
            JobProcess jobProcess = dr.getJobProcess();

            try {
                int addressId = (dr.getGeocodedAddress() != null) ? addressLogger.logAddress(dr.getGeocodedAddress().getAddress()) : 0;
                String strategy = (dr.getDistrictStrategy() != null) ? dr.getDistrictStrategy().name() : null;
                int requestId = run.query(
                    "INSERT INTO " + SCHEMA + "." + TABLE + "(apiRequestId, jobProcessId, addressId, provider, geoProvider, showMembers, showMaps, uspsValidate, skipGeocode, districtStrategy, requestTime) \n" +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) \n" +
                    "RETURNING id", new ReturnIdHandler(), (apiRequest != null) ? apiRequest.getId() : null,
                                                           (jobProcess != null) ? jobProcess.getId() : null,
                                                           (addressId > 0) ? addressId : null, dr.getProvider(), dr.getGeoProvider(), dr.isShowMembers(),
                                                           dr.isShowMaps(), dr.isUspsValidate(), dr.isSkipGeocode(), strategy, dr.getRequestTime());
                dr.setId(requestId);
                return requestId;
            }
            catch (SQLException ex) {
                logger.error("Failed to log district request!", ex);
            }
        }
        else {
            logger.error("DistrictRequest was null, cannot be logged!");
        }
        return 0;
    }
}
