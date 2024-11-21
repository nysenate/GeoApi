package gov.nysenate.sage.dao.stats.geocode;

import gov.nysenate.sage.dao.base.BasicSqlQuery;

// TODO: proper implementation
public enum GeocodeStatsQuery implements BasicSqlQuery {
    GET_TOTAL_COUNT(""),

    GET_GEOCODER_USAGE("");

    private final String sql;

    GeocodeStatsQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return this.sql;
    }
}
