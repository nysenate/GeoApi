package gov.nysenate.sage.dao.logger.geocode;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.base.ReturnIdHandler;
import gov.nysenate.sage.dao.logger.point.SqlPointLogger;
import gov.nysenate.sage.dao.logger.address.SqlAddressLogger;
import gov.nysenate.sage.model.api.ApiRequest;
import gov.nysenate.sage.model.api.GeocodeRequest;
import gov.nysenate.sage.model.job.JobProcess;
import org.apache.commons.dbutils.QueryRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.sql.SQLException;

@Repository
public class SqlGeocodeRequestLogger
{
    private static SqlAddressLogger sqlAddressLogger;
    private static SqlPointLogger sqlPointLogger;
    private static Logger logger = LoggerFactory.getLogger(SqlGeocodeRequestLogger.class);

    private static String SCHEMA = "log";
    private static String TABLE = "geocodeRequest";

    private QueryRunner run;
    private BaseDao baseDao;

    @Autowired
    public SqlGeocodeRequestLogger(SqlAddressLogger sqlAddressLogger, SqlPointLogger sqlPointLogger, BaseDao baseDao) {
        this.sqlAddressLogger = sqlAddressLogger;
        this.sqlPointLogger = sqlPointLogger;
        this.baseDao = baseDao;
        run = this.baseDao.getQueryRunner();
    }

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

                int addressId = (!geoRequest.isReverse()) ? sqlAddressLogger.logAddress(geoRequest.getAddress()) : 0;
                int pointId = (geoRequest.isReverse()) ? sqlPointLogger.logPoint(geoRequest.getPoint()) : 0;

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