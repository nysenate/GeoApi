package gov.nysenate.sage.dao.provider.shapefile;

import gov.nysenate.sage.dao.base.BasicSqlQuery;
import gov.nysenate.sage.dao.base.SqlTable;

public enum ShapefileTypeQuery implements BasicSqlQuery {
    GET_ALL_TYPE_INFO("""
            SELECT type_name, code_column
            FROM ${schema}.%s
            """.formatted(SqlTable.TYPE_INFO)),

    GET_SINGLE_TYPE_INFO(GET_ALL_TYPE_INFO.sql + "\nWHERE type_name ILIKE :typeName");

    private final String sql;

    ShapefileTypeQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return sql;
    }
}
