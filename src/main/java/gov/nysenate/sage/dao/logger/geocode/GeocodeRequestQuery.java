package gov.nysenate.sage.dao.logger.geocode;

import gov.nysenate.sage.dao.base.BasicSqlQuery;
import gov.nysenate.sage.dao.base.SqlTable;

public enum GeocodeRequestQuery implements BasicSqlQuery {
    // TODO: change "provider"
    INSERT_REQUEST("INSERT INTO ${schema}. " + SqlTable.GEOCODE_REQUEST + "(apiRequestId, jobProcessId, addressId, pointId, provider, useFallback, useCache, requestTime) \n" +
            "VALUES (:apiRequestId, :jobProcessId, :addressId, :pointId, :provider, :useFallback, :useCache, :requestTime) \n" +
            "RETURNING id")
    ;

    private final String sql;

    GeocodeRequestQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return this.sql;
    }
}
