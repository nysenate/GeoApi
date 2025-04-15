package gov.nysenate.sage.dao.stats.geocode;

import gov.nysenate.sage.dao.base.BasicSqlQuery;

import static gov.nysenate.sage.dao.base.SqlTable.GEOCODE_STATS;

public enum GeocodeStatsQuery implements BasicSqlQuery {
    INSERT_GEOCODE_STATS(
        "INSERT INTO ${schema}." + GEOCODE_STATS + " (geocoder, success) VALUES (:geocoder, :success)"
    ),

    GET_TOTAL_STATS(
        "SELECT geocoder, success, COUNT(*) FROM ${schema}." + GEOCODE_STATS + " GROUP BY geocoder, success"
    );

    private final String sql;

    GeocodeStatsQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return this.sql;
    }
}
