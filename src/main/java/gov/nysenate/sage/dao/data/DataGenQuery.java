package gov.nysenate.sage.dao.data;

import gov.nysenate.sage.dao.base.BasicSqlQuery;
import gov.nysenate.sage.dao.base.SqlTable;

public enum DataGenQuery implements BasicSqlQuery {

    SELECT_SENATE_COUNTY_CODES("select name, id from ${schema}." +SqlTable.PUBLIC_COUNTY+";"),

    SELECT_TOWN_CODES("select name, abbrev from ${schema}." + SqlTable.DISTRICT_TOWN + ";")
    ;

    private String sql;

    DataGenQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return this.sql;
    }
}
