package gov.nysenate.sage.dao.model.congressional;

import gov.nysenate.sage.dao.base.BasicSqlQuery;
import gov.nysenate.sage.dao.base.SqlTable;

public enum CongressionalQuery implements BasicSqlQuery { //${schema}." + SqlTable.PUBLIC_CONGRESSIONAL +

    GET_ALL_CONGRESSIONAL_MEMBERS("SELECT * FROM ${schema}." +SqlTable.PUBLIC_CONGRESSIONAL),

    GET_CONGRESSIONAL_MEMBER_BY_DISTRICT("SELECT * FROM ${schema}." +SqlTable.PUBLIC_CONGRESSIONAL + " WHERE district = :district"),

    INSERT_CONGRESSIONAL_MEMBER("INSERT INTO ${schema}." +SqlTable.PUBLIC_CONGRESSIONAL + " (district, memberName, memberUrl) VALUES (:district,:memberName,:memberUrl)"),

    DELETE_CONGRESSIONAL_DISTRICT("DELETE FROM ${schema}." +SqlTable.PUBLIC_CONGRESSIONAL + " WHERE district = :district");

    private final String sql;

    CongressionalQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return this.sql;
    }
}
