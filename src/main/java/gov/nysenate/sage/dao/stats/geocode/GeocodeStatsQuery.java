package gov.nysenate.sage.dao.stats.geocode;

import gov.nysenate.sage.dao.base.BasicSqlQuery;
import gov.nysenate.sage.dao.base.SqlTable;

public enum GeocodeStatsQuery implements BasicSqlQuery {

    GET_TOTAL_COUNT("SELECT COUNT(*) AS totalGeocodes,\n" +
            "COUNT( DISTINCT resultTime ) AS totalRequests,\n" +
            "COUNT(NULLIF(cacheHit, false)) AS cacheHits\n" +
            "FROM ${schema}." + SqlTable.GEOCODE_RESULT +"\n" +
            "WHERE resultTime >= :from AND resultTime <= :to"),

    GET_GEOCODER_USAGE("SELECT replace(method, 'Dao', '') AS method, COUNT(DISTINCT resultTime) AS requests\n" +
            "FROM ${schema}." + SqlTable.GEOCODE_RESULT +"\n" +
            "WHERE cacheHit = false\n" +
            "AND resultTime >= :from AND resultTime <= :to\n" +
            "GROUP BY method\n" +
            "ORDER BY requests DESC")
    ;

    private String sql;

    GeocodeStatsQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return this.sql;
    }
}
