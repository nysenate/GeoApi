package gov.nysenate.sage.dao.model.member;

import gov.nysenate.sage.dao.base.BasicSqlQuery;

public enum MemberQuery implements BasicSqlQuery {
    INSERT_MEMBER("INSERT INTO ${schema}.${memberTable} (district, member_name, member_url) VALUES (:district, :memberName, :memberUrl)"),

    DELETE_MEMBER("DELETE FROM ${schema}.${memberTable} WHERE district = :district"),

    GET_ALL_MEMBERS("SELECT * FROM ${schema}.${memberTable}");

    private final String sql;

    MemberQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return sql;
    }
}
