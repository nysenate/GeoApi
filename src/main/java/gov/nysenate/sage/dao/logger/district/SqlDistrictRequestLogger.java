package gov.nysenate.sage.dao.logger.district;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.base.ReturnIdHandler;
import gov.nysenate.sage.dao.logger.address.SqlAddressLogger;
import gov.nysenate.sage.model.api.ApiRequest;
import gov.nysenate.sage.model.api.DistrictRequest;
import gov.nysenate.sage.model.job.JobProcess;
import org.apache.commons.dbutils.QueryRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class SqlDistrictRequestLogger
{
    private static Logger logger = LoggerFactory.getLogger(SqlDistrictRequestLogger.class);
    private static SqlAddressLogger sqlAddressLogger;
    private static String SCHEMA = "log";
    private static String TABLE = "districtRequest";
    private QueryRunner run;

    private BaseDao baseDao;

    @Autowired
    public SqlDistrictRequestLogger(SqlAddressLogger sqlAddressLogger, BaseDao baseDao) {
        this.sqlAddressLogger = sqlAddressLogger;
        this.baseDao = baseDao;
        run = this.baseDao.getQueryRunner();
    }

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
                int addressId = (dr.getGeocodedAddress() != null) ? sqlAddressLogger.logAddress(dr.getGeocodedAddress().getAddress()) : 0;
                String strategy = (dr.getDistrictStrategy() != null) ? dr.getDistrictStrategy().name() : null;

                MapSqlParameterSource params = new MapSqlParameterSource();
                params.addValue("apiRequestId",(apiRequest != null) ? apiRequest.getId() : null);
                params.addValue("jobProcessId",(jobProcess != null) ? jobProcess.getId() : null);
                params.addValue("addressId",(addressId > 0) ? addressId : null);
                params.addValue("provider", dr.getProvider());
                params.addValue("geoProvider",dr.getGeoProvider());
                params.addValue("showMembers",dr.isShowMembers());
                params.addValue("showMaps",dr.isShowMaps());
                params.addValue("uspsValidate",dr.isUspsValidate());
                params.addValue("skipGeocode",dr.isSkipGeocode());
                params.addValue("districtStrategy",strategy);
                params.addValue("requestTime",dr.getRequestTime());

                List<Integer> idList = baseDao.geoApiNamedJbdcTemaplate.query(
                        DistrictRequestQuery.INSERT_REQUEST.getSql(baseDao.getLogSchema()),
                        params, new DistrictRequestIdHandler());
                dr.setId(idList.get(0));
                return idList.get(0);
            }
            catch (Exception ex) {
                logger.error("Failed to log district request!", ex);
            }
        }
        else {
            logger.error("DistrictRequest was null, cannot be logged!");
        }
        return 0;
    }

    private static class DistrictRequestIdHandler implements RowMapper<Integer> {
        @Override
        public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getInt("id");
        }
    }
}
