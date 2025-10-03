package gov.nysenate.sage.dao.model.townCity;

import gov.nysenate.sage.dao.base.BasicSqlQuery;

public enum TownCityQuery implements BasicSqlQuery {
    SELECT_ALL_("SELECT * FROM districts.town_city"),

    SELECT_ALL_REPEAT_NAMES("""
            SELECT name FROM districts.town_city
            GROUP BY name, muni_type
            HAVING COUNT(*) > 1"""),

    SELECT_VOTER_FILE_CODE("SELECT voterfile_code FROM public.town_city WHERE district_code = :code");

    private final String sql;

    TownCityQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return sql;
    }
}
