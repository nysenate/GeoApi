package gov.nysenate.sage.dao.model.member;

import gov.nysenate.sage.dao.base.BasicSqlQuery;

public enum MemberQuery implements BasicSqlQuery {
    GET_ALL_MEMBERS("SELECT * FROM ${schema}.${memberTable}"),

    GET_MEMBER_BY_DISTRICT("SELECT * FROM ${schema}.${memberTable} WHERE district = :district"),

    INSERT_MEMBER("INSERT INTO ${schema}.${memberTable} (district, memberName, memberUrl) VALUES (:district,:memberName,:memberUrl)"),

    DELETE_DISTRICT("DELETE FROM ${schema}.${memberTable} WHERE district = :district");

    private final String sql;

    MemberQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return sql;
    }
}
