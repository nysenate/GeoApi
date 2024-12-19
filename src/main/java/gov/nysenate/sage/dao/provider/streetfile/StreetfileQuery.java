package gov.nysenate.sage.dao.provider.streetfile;

import gov.nysenate.sage.dao.base.BasicSqlQuery;

public enum StreetfileQuery implements BasicSqlQuery {
    SELECT_BY_ZIP("SELECT * FROM public.streetfile WHERE zip = :zip5 ORDER BY street, bldg_low");

    private final String sql;

    StreetfileQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return "";
    }
}
