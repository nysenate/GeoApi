package gov.nysenate.sage.dao.logger.exception;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.logger.apirequest.SqlApiRequestLogger;
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
public class SqlExceptionLogger implements ExceptionLogger
{
    private static Logger logger = LoggerFactory.getLogger(SqlExceptionLogger.class);
    private SqlApiRequestLogger sqlApiRequestLogger;
    private BaseDao baseDao;
    Marker fatal = MarkerFactory.getMarker("FATAL");

    @Autowired
    public SqlExceptionLogger(BaseDao baseDao, SqlApiRequestLogger sqlApiRequestLogger) {
        this.sqlApiRequestLogger = sqlApiRequestLogger;
        this.baseDao = baseDao;
    }

    /** {@inheritDoc} */
    public void logException(Exception ex, Timestamp catchTime, Integer apiRequestId)
    {
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("apiRequestId",apiRequestId);
            params.addValue("type",ex.getClass().getName());
            params.addValue("message",ex.getMessage());
            params.addValue("stackTrace",FormatUtil.toJsonString(sw.toString()));
            params.addValue("catchTime",catchTime);

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
