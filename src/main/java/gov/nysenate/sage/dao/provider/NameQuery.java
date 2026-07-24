package gov.nysenate.sage.dao.provider;

import gov.nysenate.sage.dao.base.BasicSqlQuery;

public enum NameQuery implements BasicSqlQuery {
    GET_SHAPEFILE_DATA("""
            SELECT ${nameColumn:-NULL::text} AS name, ${codeColumn} AS code
            FROM ${schema}.${type}
            GROUP BY name, code"""),

    GET_STREETFILE_CODES("""
            SELECT DISTINCT ${codeColumn} AS code
            FROM ${schema}.streetfile
            WHERE ${codeColumn} IS NOT NULL
            """);

    private final String query;

    NameQuery(String query) {
        this.query = query;
    }

    @Override
    public String getSql() {
        return query;
    }
}
