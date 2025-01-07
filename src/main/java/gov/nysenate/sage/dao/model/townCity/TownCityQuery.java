package gov.nysenate.sage.dao.model.townCity;

import gov.nysenate.sage.dao.base.BasicSqlQuery;

public enum TownCityQuery implements BasicSqlQuery {
    SELECT_ALL("SELECT * FROM districts.town_city");

    private final String sql;

    TownCityQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return sql;
    }
}
