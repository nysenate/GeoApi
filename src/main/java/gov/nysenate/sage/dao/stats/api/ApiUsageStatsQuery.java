package gov.nysenate.sage.dao.stats.api;

import gov.nysenate.sage.dao.base.BasicSqlQuery;
import gov.nysenate.sage.dao.base.SqlTable;

public enum ApiUsageStatsQuery implements BasicSqlQuery {

    GET_USAGE_STATS("SELECT date_trunc(:requestInterval, requestTime) AS requestInterval, COUNT(*) AS requests \n" +
            "FROM ${schema}."+ SqlTable.API_REQUEST + " AS ar \n" +
            "WHERE ar.requestTime >= :from AND ar.requestTime <= :to \n" +
            "GROUP BY date_trunc(:requestInterval, requestTime), ar.requestTime\n" +
            "ORDER BY requestInterval")
            ;

    private String sql;

    ApiUsageStatsQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return this.sql;
    }
}
