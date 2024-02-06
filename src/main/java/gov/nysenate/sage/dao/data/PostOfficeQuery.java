package gov.nysenate.sage.dao.data;

import gov.nysenate.sage.dao.base.BasicSqlQuery;
import gov.nysenate.sage.dao.base.SqlTable;

public enum PostOfficeQuery implements BasicSqlQuery {
    CLEAR_TABLE("DELETE FROM ${schema}." + SqlTable.POST_OFFICE),
    GET_ALL_POST_OFFICES("SELECT * FROM ${schema}." + SqlTable.POST_OFFICE),
    ADD_ADDRESS(
            "INSERT INTO ${schema}." + SqlTable.POST_OFFICE + " (delivery_zip, street_with_num, city, zip5, zip4)\n" +
            "VALUES (:deliveryZip, :streetWithNum, :city, :zip5, :zip4)"
    );

    private final String sql;

    PostOfficeQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return sql;
    }
}
