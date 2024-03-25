package gov.nysenate.sage.dao.model.county;

import gov.nysenate.sage.dao.base.BasicSqlQuery;
import gov.nysenate.sage.dao.base.SqlTable;

public enum CountyQuery implements BasicSqlQuery {
    GET_ALL_COUNTIES("SELECT * FROM ${schema}." + SqlTable.PUBLIC_COUNTY),
    GET_COUNTY_BY_ID("SELECT * FROM ${schema}." + SqlTable.PUBLIC_COUNTY + " WHERE senate_code = :senateCode"),
    GET_COUNTY_BY_NAME("SELECT * FROM ${schema}." + SqlTable.PUBLIC_COUNTY + " WHERE LOWER(name) = LOWER(:name)"),
    GET_COUNTY_BY_FIPS_CODE("SELECT * FROM ${schema}." + SqlTable.PUBLIC_COUNTY + " WHERE fips_code = :fipsCode ");

    private final String sql;

    CountyQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return this.sql;
    }
}
