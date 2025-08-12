package gov.nysenate.sage.dao.provider.geocache;

import gov.nysenate.sage.dao.base.BasicSqlQuery;

public enum SqlGeocacheQuery implements BasicSqlQuery {
    SELECT_CACHE_ENTRY("""
            SELECT gc.*, ST_Y(latlon) AS lat, ST_X(latlon) AS lon
            FROM public.geocache AS gc
            WHERE gc.bldg_id = :bldgId AND gc.street = :street AND gc.postal_city = :postalCity AND gc.zip5 = :zip5
            """
    ),

    INSERT_CACHE_ENTRY(
            "INSERT INTO public.geocache (bldg_id, street, postal_city, state, zip5, latlon, method, quality) " +
            "VALUES (:bldgId, :street, :postalCity, :state, :zip5, ST_GeomFromText( :latlon ), :method, :quality)"
    ),

    UPDATE_CACHE_ENTRY("""
            UPDATE public.geocache
            SET latlon = ST_GeomFromText(:latlon), method = :method, quality = :quality, updated = NOW()
            WHERE bldg_id = :bldgId AND street = :street AND postal_city = :postalCity AND zip5 = :zip5"""
    );

    private final String sql;

    SqlGeocacheQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return sql;
    }
}
