package gov.nysenate.sage.dao.data;

import gov.nysenate.sage.dao.base.BasicSqlQuery;
import gov.nysenate.sage.dao.base.SqlTable;

public enum PostOfficeQuery implements BasicSqlQuery {
    CLEAR_TABLE("DELETE FROM ${schema}." + SqlTable.POST_OFFICE),
    ADD_ADDRESS(
            "INSERT INTO ${schema}." + SqlTable.POST_OFFICE + " (delivery_zip, bldg_id, street, city, zip5, zip4)\n" +
            "VALUES (:deliveryZip, :bldgId, :street, :city, :zip5, :zip4)"
    ),
    GET_ADDRESSES_BY_DELIVERY_ZIP(
            "SELECT * FROM ${schema}." + SqlTable.POST_OFFICE +
            " WHERE delivery_zip = :deliveryZip"
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
