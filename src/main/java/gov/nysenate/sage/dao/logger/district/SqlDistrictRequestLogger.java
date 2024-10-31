package gov.nysenate.sage.dao.logger.district;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.logger.address.SqlAddressLogger;
import gov.nysenate.sage.model.api.ApiRequest;
import gov.nysenate.sage.model.api.DistrictRequest;
import gov.nysenate.sage.model.job.JobProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class SqlDistrictRequestLogger implements DistrictRequestLogger {
    private static final Logger logger = LoggerFactory.getLogger(SqlDistrictRequestLogger.class);
    private final SqlAddressLogger sqlAddressLogger;
    private final BaseDao baseDao;

    @Autowired
    public SqlDistrictRequestLogger(SqlAddressLogger sqlAddressLogger, BaseDao baseDao) {
        this.sqlAddressLogger = sqlAddressLogger;
        this.baseDao = baseDao;
    }

    /** {@inheritDoc} */
    public int logDistrictRequest(DistrictRequest dr) {
        if (dr != null) {
            ApiRequest apiRequest = dr.getApiRequest();
            JobProcess jobProcess = dr.getJobProcess();

            try {
                int addressId = (dr.getGeocodedAddress() != null) ? sqlAddressLogger.logAddress(dr.getGeocodedAddress().getAddress()) : 0;
                String strategy = (dr.getDistrictStrategy() != null) ? dr.getDistrictStrategy().name() : null;

                // TODO: cleanup now that showMaps and showMembers are gone
                var params = new MapSqlParameterSource()
                        .addValue("apiRequestId", (apiRequest != null) ? apiRequest.getId() : null)
                        .addValue("jobProcessId", (jobProcess != null) ? jobProcess.getId() : null)
                        .addValue("addressId", (addressId > 0) ? addressId : null)
                        .addValue("provider", dr.getProvider())
                        .addValue("geoProvider", dr.getGeoProvider())
                        .addValue("showMembers", false)
                        .addValue("showMaps", false)
                        .addValue("uspsValidate", dr.isUspsValidate())
                        .addValue("skipGeocode", dr.isSkipGeocode())
                        .addValue("districtStrategy", strategy)
                        .addValue("requestTime", dr.getRequestTime());

                List<Integer> idList = baseDao.geoApiNamedJbdcTemplate.query(
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
