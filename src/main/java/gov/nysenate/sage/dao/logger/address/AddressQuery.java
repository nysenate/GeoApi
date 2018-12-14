package gov.nysenate.sage.dao.logger.address;

import gov.nysenate.sage.dao.base.BasicSqlQuery;
import gov.nysenate.sage.dao.base.SqlTable;

public enum AddressQuery implements BasicSqlQuery {

    INSERT_ADDRESS("INSERT INTO ${schema}." + SqlTable.ADDRESS + "(addr1, addr2, city, state, zip5, zip4) \n" +
            "VALUES (:addr1, :addr2, :city, :state, :zip5, :zip4) \n" +
            "RETURNING id"),
    GET_ADDRESS_ID("SELECT * FROM ${schema}." + SqlTable.ADDRESS + "\n" +
            "WHERE addr1 ILIKE :addr1 AND addr2 ILIKE :addr2 AND city ILIKE :city AND state ILIKE :state AND " +
            "zip5 ILIKE :zip5 AND zip4 ILIKE :zip4 " +
            "LIMIT 1");


    private String sql;

    AddressQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return this.sql;
    }
}
