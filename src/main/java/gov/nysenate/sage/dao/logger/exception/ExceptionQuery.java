package gov.nysenate.sage.dao.logger.exception;

import gov.nysenate.sage.dao.base.BasicSqlQuery;
import gov.nysenate.sage.dao.base.SqlTable;

public enum ExceptionQuery implements BasicSqlQuery {

    INSERT_EXCEPTION("INSERT INTO ${schema}." + SqlTable.EXCEPTION + "(apiRequestId, type, message, stackTrace, catchTime) \n" +
            "VALUES (:apiRequestId, :type, :message, :stackTrace, :catchTime) \n" +
            "RETURNING id"),

    ;

    private String sql;

    ExceptionQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return this.sql;
    }
}
