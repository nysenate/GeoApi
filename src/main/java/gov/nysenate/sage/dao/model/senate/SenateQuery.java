package gov.nysenate.sage.dao.model.senate;

import gov.nysenate.sage.dao.base.BasicSqlQuery;
import gov.nysenate.sage.dao.base.SqlTable;

public enum SenateQuery implements BasicSqlQuery  {

    INSERT_SENATOR("INSERT INTO ${schema}." + SqlTable.SENATOR + " (district, name, data) VALUES (:district,:name,:data)"),

    INSERT_SENATE("INSERT INTO ${schema}." + SqlTable.PUBLIC_SENATE +" (district, url) VALUES (:district,:url)"),

    CLEAR_SENATE("DELETE FROM ${schema}." + SqlTable.PUBLIC_SENATE),

    CLEAR_SENATORS("DELETE FROM ${schema}." + SqlTable.PUBLIC_SENATE),

    DELETE_SENATOR_BY_DISTRICT("DELETE FROM ${schema}." + SqlTable.SENATOR + " WHERE district = :district"),

    GET_ALL_SENATORS("SELECT * FROM ${schema}." + SqlTable.SENATOR),

    ;

    private String sql;

    SenateQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return this.sql;
    }
}
