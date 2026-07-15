package gov.nysenate.sage.dao.model.county;

import gov.nysenate.sage.dao.base.BasicSqlQuery;
import gov.nysenate.sage.dao.base.SqlTable;

public enum CountyQuery implements BasicSqlQuery {
    GET_ALL_COUNTIES("SELECT pc.name AS name, dc.${codeColumn} AS code, pc.voterfile_code, pc.link\n" +
            "FROM ${schema}." + SqlTable.PUBLIC_COUNTY + " pc\n" +
            "JOIN districts.county dc\n" +
            "ON pc.name = dc.${nameColumn}");

    private final String sql;

    CountyQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return this.sql;
    }
}
