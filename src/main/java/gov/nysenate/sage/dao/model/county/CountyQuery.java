package gov.nysenate.sage.dao.model.county;

import gov.nysenate.sage.dao.base.BasicSqlQuery;
import gov.nysenate.sage.dao.base.SqlTable;

public enum CountyQuery implements BasicSqlQuery { //${schema}." + SqlTable.PUBLIC_COUNTY

    GET_ALL_COUNTIES("SELECT id, name, fips_code AS fipsCode FROM ${schema}." + SqlTable.PUBLIC_COUNTY),

    GET_COUNTY_BY_ID("SELECT id, name, fips_code AS fipsCode FROM ${schema}." + SqlTable.PUBLIC_COUNTY + " WHERE id = :id"),

    GET_COUNTY_BY_NAME("SELECT id, name, fips_code AS fipsCode FROM ${schema}." + SqlTable.PUBLIC_COUNTY + " WHERE LOWER(name) = LOWER(:name)"),

    GET_COUNTY_BY_FIPS_CODE("SELECT id, name, fips_code AS fipsCode FROM ${schema}." + SqlTable.PUBLIC_COUNTY + " WHERE fips_code = :fipsCode "),

    ;

    private String sql;

    CountyQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return this.sql;
    }
}
