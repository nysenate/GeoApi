package gov.nysenate.sage.dao.logger.exception;

import java.sql.Timestamp;

public interface ExceptionLogger {
    /**
     * Logs an uncaught exception to the database.
     * @param ex            The exception
     * @param catchTime     The time the unhandled exception was eventually caught
     * @param apiRequestId  Associated apiRequestId set at the time when exception arose
     */
    public void logException(Exception ex, Timestamp catchTime, Integer apiRequestId);
}
