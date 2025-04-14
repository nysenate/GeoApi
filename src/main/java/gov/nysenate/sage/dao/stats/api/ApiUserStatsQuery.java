package gov.nysenate.sage.dao.stats.api;

import gov.nysenate.sage.dao.base.BasicSqlQuery;
import gov.nysenate.sage.dao.base.SqlTable;

public enum ApiUserStatsQuery implements BasicSqlQuery {
    GET_REQUESTS("SELECT api_user_id, service, request, request_time\n" +
            "FROM ${schema}." + SqlTable.API_REQUEST + " ar\n" +
            "WHERE :from <= request_time AND request_time <= :to");

    private final String sql;

    ApiUserStatsQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return this.sql;
    }
}
