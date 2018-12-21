package gov.nysenate.sage.dao.model.job;

import gov.nysenate.sage.dao.base.BasicSqlQuery;
import gov.nysenate.sage.dao.base.SqlTable;

public enum JobUserQuery implements BasicSqlQuery { //${schema}." + SqlTable.USER

    GET_ALL_JOB_USERS("SELECT * FROM ${schema}." + SqlTable.USER),

    GET_JOB_USER_BY_ID("SELECT * FROM ${schema}." + SqlTable.USER + " WHERE id = :id"),

    GET_JOB_USER_BY_EMAIL("SELECT * FROM ${schema}." + SqlTable.USER + " WHERE email = :email"),

    INSERT_JOB_USER("INSERT INTO ${schema}." + SqlTable.USER + "(email,password,firstname,lastname,active,admin) VALUES (:email,:password,:firstname,:lastname,:active,:admin)"),

    REMOVE_JOB_USER("DELETE FROM ${schema}." + SqlTable.USER + " WHERE id = :id"),
    ;

    private String sql;

    JobUserQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return this.sql;
    }
}
