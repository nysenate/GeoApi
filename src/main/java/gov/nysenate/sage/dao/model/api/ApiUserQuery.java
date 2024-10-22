package gov.nysenate.sage.dao.model.api;

import gov.nysenate.sage.dao.base.BasicSqlQuery;
import gov.nysenate.sage.dao.base.SqlTable;

public enum ApiUserQuery implements BasicSqlQuery {
    GET_API_USER_BY_ID("SELECT * FROM ${schema}." + SqlTable.API_USER + " WHERE id = :id"),

    GET_API_USER_BY_KEY("SELECT * FROM ${schema}." + SqlTable.API_USER + " WHERE apikey = :apikey"),

    GET_ALL_API_USERS("SELECT * FROM ${schema}." + SqlTable.API_USER),

    INSERT_API_USER("INSERT INTO ${schema}." + SqlTable.API_USER + " (apikey,name,description,admin) VALUES (:apikey,:name,:description,:admin)"),

    REMOVE_API_USER("DELETE FROM ${schema}." + SqlTable.API_USER + " WHERE id = :id");

    private final String sql;

    ApiUserQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return this.sql;
    }
}
