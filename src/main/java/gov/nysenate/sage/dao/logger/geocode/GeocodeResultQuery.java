package gov.nysenate.sage.dao.logger.geocode;

import gov.nysenate.sage.dao.base.BasicSqlQuery;
import gov.nysenate.sage.dao.base.SqlTable;

public enum GeocodeResultQuery implements BasicSqlQuery {
    INSERT_RESULT("INSERT INTO ${schema}." + SqlTable.GEOCODE_RESULT + "(geocodeRequestId, success, cacheHit, addressId, method, quality, latLonId, resultTime) \n" +
            "VALUES (:geocodeRequestId, :success, :cacheHit, :addressId, :method, :quality, :latLonId, :resultTime) \n" +
            "RETURNING id");

    private final String sql;

    GeocodeResultQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return this.sql;
    }
}
