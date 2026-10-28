package gov.nysenate.sage.dao.provider;

import gov.nysenate.sage.dao.base.BasicSqlQuery;

public enum DistrictInfoQuery implements BasicSqlQuery {
    GET_SHAPEFILE_DATA("""
            SELECT *
            FROM ${schema}.${type}
            """),

    // Note that getting codes instead of IDs is correct: the streetfile stores codes.
    GET_STREETFILE_CODES("""
            SELECT DISTINCT ${codeColumn} AS code
            FROM ${schema}.streetfile
            WHERE ${codeColumn} IS NOT NULL
            """);

    private final String query;

    DistrictInfoQuery(String query) {
        this.query = query;
    }

    @Override
    public String getSql() {
        return query;
    }
}
