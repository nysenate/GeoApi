package gov.nysenate.sage.dao.data;

import gov.nysenate.sage.dao.base.BasicSqlQuery;
import gov.nysenate.sage.dao.base.SqlTable;

public enum PostOfficeQuery implements BasicSqlQuery {
    GET_ADDRESS_FROM_ZIP("SELECT address FROM ${schema}." + SqlTable.POST_OFFICE + " WHERE po_box_zip5 = :zip5");

    private final String sql;

    PostOfficeQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return sql;
    }
}
