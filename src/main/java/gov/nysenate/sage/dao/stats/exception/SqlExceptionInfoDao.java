package gov.nysenate.sage.dao.stats.exception;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.logger.apirequest.SqlApiRequestLogger;
import gov.nysenate.sage.dao.stats.deployment.SqlDeploymentStatsDao;
import gov.nysenate.sage.model.stats.ExceptionInfo;
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
public class SqlExceptionInfoDao {
    private static Logger logger = LoggerFactory.getLogger(SqlDeploymentStatsDao.class);

    private SqlApiRequestLogger sqlApiRequestLogger;
    private BaseDao baseDao;

    @Autowired
    public SqlExceptionInfoDao(SqlApiRequestLogger sqlApiRequestLogger, BaseDao baseDao) {
        this.sqlApiRequestLogger = sqlApiRequestLogger;
        this.baseDao = baseDao;
    }

    /** {@inheritDoc} */
    public List<ExceptionInfo> getExceptionInfoList(Boolean excludeHidden) {
        try {

            if (excludeHidden) {
                return baseDao.geoApiNamedJbdcTemplate.query(
                        ExceptionInfoQuery.SELECT_EXCEPTIONS_WITH_HIDDEN.getSql(baseDao.getLogSchema()),
                        new ExceptionInfoListHandler(this.sqlApiRequestLogger));
            }
            else {
                return baseDao.geoApiNamedJbdcTemplate.query(
                        ExceptionInfoQuery.SELECT_EXCEPTIONS_HIDDEN_FALSE.getSql(baseDao.getLogSchema()),
                        new ExceptionInfoListHandler(this.sqlApiRequestLogger));
            }
        } catch (Exception ex) {
            logger.error("Failed to retrieve exception info list!", ex);
        }
        return null;
    }

    /** {@inheritDoc} */
    public int hideExceptionInfo(int id) {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("id", id);
            return baseDao.geoApiNamedJbdcTemplate.update(
                    ExceptionInfoQuery.HIDE_EXCEPTION.getSql(baseDao.getLogSchema()), params);
        } catch (Exception ex) {
            logger.error("Failed to hide exception with id: " + id, ex);
        }
        return 0;
    }

    /**
     * Handler implementation to create List<ExceptionInfo> from the result set.
     */
    private static class ExceptionInfoListHandler implements RowMapper<ExceptionInfo> {
        private SqlApiRequestLogger sqlApiRequestLogger;

        public ExceptionInfoListHandler(SqlApiRequestLogger sqlApiRequestLogger) {
            this.sqlApiRequestLogger = sqlApiRequestLogger;
        }

        public ExceptionInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
            ExceptionInfo ei = new ExceptionInfo();
            ei.setId(rs.getInt("id"));
            ei.setApiRequest(sqlApiRequestLogger.getApiRequest(rs.getInt("apiRequestId")));
            ei.setExceptionType(rs.getString("type"));
            ei.setMessage(rs.getString("message"));
            ei.setStackTrace(rs.getString("stackTrace"));
            ei.setCatchTime(rs.getTimestamp("catchTime"));
            return ei;
        }
    }
}
