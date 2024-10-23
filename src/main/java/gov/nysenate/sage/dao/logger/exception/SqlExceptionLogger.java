package gov.nysenate.sage.dao.logger.exception;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.util.FormatUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

@Repository
public class SqlExceptionLogger {
    private static final Logger logger = LoggerFactory.getLogger(SqlExceptionLogger.class);
    private static final Marker fatal = MarkerFactory.getMarker("FATAL");
    private final BaseDao baseDao;

    @Autowired
    public SqlExceptionLogger(BaseDao baseDao) {
        this.baseDao = baseDao;
    }

    /**
     * Logs an uncaught exception to the database.
     * @param ex            The exception
     * @param catchTime     The time the unhandled exception was eventually caught
     * @param apiRequestId  Associated apiRequestId set at the time when exception arose
     */
    public void logException(Exception ex, Timestamp catchTime, Integer apiRequestId) {
        try {
            var sw = new StringWriter();
            var pw = new PrintWriter(sw);
            ex.printStackTrace(pw);

            var params = new MapSqlParameterSource()
                    .addValue("apiRequestId", apiRequestId)
                    .addValue("type", ex.getClass().getName())
                    .addValue("message", ex.getMessage())
                    .addValue("stackTrace", FormatUtil.toJsonString(sw.toString()))
                    .addValue("catchTime", catchTime);
            baseDao.geoApiNamedJbdcTemplate.query(ExceptionQuery.INSERT_EXCEPTION.getSql(baseDao.getLogSchema()),
                    params, new ExceptionIdHandler());
        }
        catch (Exception ex2) {
            logger.error(fatal, "Failed to log unhandled exception!", ex2);
        }
    }

    private static class ExceptionIdHandler implements RowMapper<Integer> {
        @Override
        public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getInt("id");
        }
    }
}
