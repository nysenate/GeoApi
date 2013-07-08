package gov.nysenate.sage.dao.logger;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.base.ReturnIdHandler;
import gov.nysenate.sage.model.stats.ExceptionStats;
import gov.nysenate.sage.util.FormatUtil;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static gov.nysenate.sage.model.stats.ExceptionStats.ExceptionInfo;

public class ExceptionLogger extends BaseDao
{
    private static Logger logger = Logger.getLogger(ExceptionLogger.class);
    private static ApiRequestLogger apiRequestLogger = new ApiRequestLogger();
    private static String SCHEMA = "log";
    private static String TABLE = "exceptions";
    private QueryRunner run = getQueryRunner();

    /**
     * Logs an uncaught exception to the database.
     * @param ex            The exception
     * @param catchTime     The time the unhandled exception was eventually caught
     * @param apiRequestId  Associated apiRequestId set at the time when exception arose
     */
    public void logException(Exception ex, Timestamp catchTime, Integer apiRequestId)
    {
        String sql = "INSERT INTO " + SCHEMA + "." + TABLE + "(apiRequestId, type, message, stackTrace, catchTime) \n" +
                     "VALUES (?, ?, ?, ?, ?) \n" +
                     "RETURNING id";
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);

            run.query(sql, new ReturnIdHandler(), apiRequestId, ex.getClass().getName(), ex.getMessage(), FormatUtil.toJsonString(sw.toString()), catchTime);
        }
        catch (SQLException ex2) {
            logger.fatal("Failed to log unhandled exception!", ex2);
        }
    }

    /**
     *
     * @param from
     * @param to
     * @return
     */
    public List<ExceptionInfo> getExceptions(Timestamp from, Timestamp to)
    {
        String sql = "SELECT * \n" +
                     "FROM " + SCHEMA + "." + TABLE + "\n" +
                     "WHERE catchTime >= ? AND catchTime <= ?";
        try {
            return run.query(sql, new ExceptionInfoListHandler(), from , to);
        }
        catch (SQLException ex) {
            logger.error("Failed to retrieve ExceptionInfo list!", ex);
        }
        return null;
    }

    private static class ExceptionInfoListHandler implements ResultSetHandler<List<ExceptionInfo>>
    {
        @Override
        public List<ExceptionInfo> handle(ResultSet rs) throws SQLException {
            List<ExceptionInfo> exceptionInfoList = new ArrayList<>();
            while (rs.next()) {
                int apiRequestId = rs.getInt("apiRequestId");
                ExceptionInfo exceptionInfo = new ExceptionInfo();
                exceptionInfo.setApiRequest(apiRequestLogger.getApiRequest(apiRequestId));
                exceptionInfo.setExceptionType(rs.getString("type"));
                exceptionInfo.setMessage(rs.getString("message"));
                exceptionInfo.setStackTrace(rs.getString("stacktrace"));
                exceptionInfo.setCatchTime(rs.getTimestamp("catchtime"));
            }
            return exceptionInfoList;
        }
    }
}
