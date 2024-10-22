package gov.nysenate.sage.dao.provider.geocache;

import gov.nysenate.sage.dao.base.BasicSqlQuery;

public enum SqlGeocacheQuery implements BasicSqlQuery {
    SELECT_CACHE_ENTRY("""
            SELECT gc.*, ST_Y(latlon) AS lat, ST_X(latlon) AS lon
            FROM cache.geocache AS gc
            WHERE gc.bldgnum = :bldgnum AND gc.street = :street AND gc.location = :location AND gc.zip5 = :zip5
            LIMIT 1"""
    ),

    INSERT_CACHE_ENTRY(
            "INSERT INTO cache.geocache (bldgnum, street, location, zip5, zip4, latlon, method, quality) " +
            "VALUES (:bldgnum, :street, :location, :zip5, :zip4, ST_GeomFromText( :latlon ), :method, :quality)"),

    UPDATE_CACHE_ENTRY("UPDATE cache.geocache " +
            "SET latlon = ST_GeomFromText(:latlon), method = :method, quality = :quality, zip4 = :zip4, updated = now() " +
            "WHERE bldgnum = :bldgnum AND street = :street AND location = :location AND zip5 = :zip5");

    private final String sql;

    SqlGeocacheQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return sql;
    }
}
