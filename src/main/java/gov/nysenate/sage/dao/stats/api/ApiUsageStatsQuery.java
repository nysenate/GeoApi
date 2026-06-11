package gov.nysenate.sage.dao.stats.api;

import gov.nysenate.sage.dao.base.BasicSqlQuery;
import gov.nysenate.sage.dao.base.SqlTable;

public enum ApiUsageStatsQuery implements BasicSqlQuery {
    GET_USAGE_STATS("SELECT date_trunc('HOUR', request_time) AS request_hour, COUNT(*) AS requests\n" +
            "FROM ${schema}."+ SqlTable.API_REQUEST + " AS ar\n" +
            "WHERE :from <= ar.request_time AND ar.request_time <= :to\n" +
            "GROUP BY request_hour\n" +
            "ORDER BY request_hour");

    private final String sql;

    ApiUsageStatsQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return this.sql;
    }
}
