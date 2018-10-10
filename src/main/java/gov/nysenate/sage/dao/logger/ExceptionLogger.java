package gov.nysenate.sage.dao.logger;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.base.ReturnIdHandler;
import gov.nysenate.sage.util.FormatUtil;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.sql.Timestamp;

public class ExceptionLogger extends BaseDao
{
    private static Logger logger = LogManager.getLogger(ExceptionLogger.class);
    private static ApiRequestLogger apiRequestLogger = new ApiRequestLogger();
    private static String SCHEMA = "log";
    private static String TABLE = "exception";
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
}
