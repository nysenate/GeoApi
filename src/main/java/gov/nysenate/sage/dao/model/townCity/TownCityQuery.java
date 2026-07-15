package gov.nysenate.sage.dao.model.townCity;

import gov.nysenate.sage.dao.base.BasicSqlQuery;

public enum TownCityQuery implements BasicSqlQuery {
    SELECT_ALL("""
            SELECT dtc.*, ptc.voterfile_code
            FROM districts.town_city dtc
            LEFT JOIN public.town_city ptc
                ON dtc.${nameColumn} = ptc.name
                AND dtc.county = ptc.county
                AND dtc.muni_type = ptc.muni_type"""),

    SELECT_ALL_REPEAT_NAMES("""
            SELECT name FROM districts.town_city
            GROUP BY name, muni_type
            HAVING COUNT(*) > 1""");

    private final String sql;

    TownCityQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return sql;
    }
}
