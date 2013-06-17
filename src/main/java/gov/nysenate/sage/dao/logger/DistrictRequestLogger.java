package gov.nysenate.sage.dao.logger;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.base.ReturnIdHandler;
import gov.nysenate.sage.model.api.ApiRequest;
import gov.nysenate.sage.model.api.DistrictRequest;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.log4j.Logger;

import java.sql.SQLException;

public class DistrictRequestLogger extends BaseDao
{
    private static Logger logger = Logger.getLogger(DistrictRequestLogger.class);
    private static AddressLogger addressLogger = new AddressLogger();
    private static String SCHEMA = "log";
    private static String TABLE = "districtRequests";
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
            int addressId = addressLogger.logAddress(dr.getAddress());
            try {
                String strategy = (dr.getDistrictStrategy() != null) ? dr.getDistrictStrategy().name() : null;
                int requestId = run.query(
                    "INSERT INTO " + SCHEMA + "." + TABLE + "(apiRequestId, addressId, provider, geoProvider, showMembers, showMaps, uspsValidate, skipGeocode, districtStrategy, requestTime) \n" +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) \n" +
                    "RETURNING id", new ReturnIdHandler(), apiRequest.getId(), addressId, dr.getProvider(), dr.getGeoProvider(), dr.isShowMembers(), dr.isShowMaps(), dr.isUspsValidate(), dr.isSkipGeocode(), strategy, dr.getRequestTime());
                dr.setId(requestId);
                return requestId;
            }
            catch (SQLException ex) {
                logger.error("Failed to log district request!", ex);
            }
        }
        return -1;
    }
}
