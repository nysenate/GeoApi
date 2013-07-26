package gov.nysenate.sage.dao.logger;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.base.ReturnIdHandler;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.api.ApiRequest;
import gov.nysenate.sage.model.api.BatchGeocodeRequest;
import gov.nysenate.sage.model.api.GeocodeRequest;
import gov.nysenate.sage.model.job.JobProcess;
import gov.nysenate.sage.model.job.JobRequest;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class GeocodeRequestLogger extends BaseDao
{
    private static Logger logger = Logger.getLogger(GeocodeRequestLogger.class);
    private static AddressLogger addressLogger = new AddressLogger();
    private static PointLogger pointLogger = new PointLogger();

    private static String SCHEMA = "log";
    private static String TABLE = "geocodeRequest";

    private QueryRunner run = getQueryRunner();

    /**
    * Log a GeocodeRequest to the database
    * @param geoRequest
    * @return id of geocode request. This id is set to the supplied GeocodeRequest as well.
    */
    public int logGeocodeRequest(GeocodeRequest geoRequest)
    {
        if (geoRequest != null) {
            try {
                ApiRequest apiRequest = geoRequest.getApiRequest();
                JobProcess jobProcess = geoRequest.getJobProcess();

                int addressId = (!geoRequest.isReverse()) ? addressLogger.logAddress(geoRequest.getAddress()) : 0;
                int pointId = (geoRequest.isReverse()) ? pointLogger.logPoint(geoRequest.getPoint()) : 0;

                int requestId = run.query(
                          "INSERT INTO " + SCHEMA + "." + TABLE + "(apiRequestId, jobProcessId, addressId, pointId, provider, useFallback, useCache, requestTime) \n" +
                          "VALUES (?, ?, ?, ?, ?, ?, ?, ?) \n" +
                          "RETURNING id", new ReturnIdHandler(), (apiRequest != null) ? apiRequest.getId() : null, (jobProcess != null) ? jobProcess.getId() : null,
                                                                 (addressId > 0) ? addressId : null, (pointId > 0) ? pointId : null, geoRequest.getProvider(), geoRequest.isUseFallback(),
                                                                 geoRequest.isUseCache(), geoRequest.getRequestTime());
                geoRequest.setId(requestId);
                return requestId;
            }
            catch (SQLException ex) {
                logger.error("Failed to log geocode request!", ex);
            }
        }
        return 0;
    }
}