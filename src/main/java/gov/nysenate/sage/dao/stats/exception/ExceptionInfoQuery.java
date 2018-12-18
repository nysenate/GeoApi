package gov.nysenate.sage.dao.stats.exception;

import gov.nysenate.sage.dao.base.BasicSqlQuery;
import gov.nysenate.sage.dao.base.SqlTable;

public enum ExceptionInfoQuery implements BasicSqlQuery {

    SELECT_EXCEPTIONS_HIDDEN_FALSE("SELECT * FROM ${schema}."+ SqlTable.EXCEPTION + "\n" +
            "WHERE hidden = false \n" +
            "ORDER BY catchTime DESC"),

    SELECT_EXCEPTIONS_WITH_HIDDEN("SELECT * FROM ${schema}."+ SqlTable.EXCEPTION + "\n" +
            "ORDER BY catchTime DESC"),

    HIDE_EXCEPTION("UPDATE ${schema}." + SqlTable.EXCEPTION + "\n" +
            "SET hidden = true \n" +
            "WHERE id = :id")

            ;

    private String sql;

    ExceptionInfoQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return this.sql;
    }
}
