package gov.nysenate.sage.dao.logger.geocode;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.logger.address.SqlAddressLogger;
import gov.nysenate.sage.dao.logger.point.SqlPointLogger;
import gov.nysenate.sage.model.api.ApiRequest;
import gov.nysenate.sage.model.api.SingleGeocodeRequest;
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
public class SqlGeocodeRequestLogger {
    private static final Logger logger = LoggerFactory.getLogger(SqlGeocodeRequestLogger.class);
    private final SqlAddressLogger sqlAddressLogger;
    private final SqlPointLogger sqlPointLogger;
    private final BaseDao baseDao;

    @Autowired
    public SqlGeocodeRequestLogger(SqlAddressLogger sqlAddressLogger, SqlPointLogger sqlPointLogger, BaseDao baseDao) {
        this.sqlAddressLogger = sqlAddressLogger;
        this.sqlPointLogger = sqlPointLogger;
        this.baseDao = baseDao;
    }

    /** {@inheritDoc} */
    public int logGeocodeRequest(SingleGeocodeRequest geoRequest) {
        if (geoRequest != null) {
            try {
                ApiRequest apiRequest = geoRequest.getApiRequest();
                JobProcess jobProcess = geoRequest.getJobProcess();

                int addressId = (!geoRequest.isReverse()) ? sqlAddressLogger.logAddress(geoRequest.getAddress()) : 0;
                int pointId = (geoRequest.isReverse()) ? sqlPointLogger.logPoint(geoRequest.getPoint()) : 0;

                var params = new MapSqlParameterSource()
                        .addValue("apiRequestId", (apiRequest != null) ? apiRequest.getId() : null)
                        .addValue("jobProcessId", (jobProcess != null) ? jobProcess.getId() : null)
                        .addValue("addressId", (addressId > 0) ? addressId : null)
                        .addValue("pointId", (pointId > 0) ? pointId : null)
                        .addValue("providers", geoRequest.getGeocoders())
                        .addValue("requestTime", geoRequest.getRequestTime());

                List<Integer> idList = baseDao.geoApiNamedJbdcTemplate.query(
                        GeocodeRequestQuery.INSERT_REQUEST.getSql(baseDao.getLogSchema()), params,
                        new GeocodeRequestIdHandler());
                geoRequest.setId(idList.get(0));
                return idList.get(0);
            }
            catch (Exception ex) {
                logger.error("Failed to log geocode request!", ex);
            }
        }
        return 0;
    }

    private static class GeocodeRequestIdHandler implements RowMapper<Integer> {
        @Override
        public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getInt("id");
        }
    }
}