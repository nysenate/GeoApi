package gov.nysenate.sage.dao.stats.api;

import gov.nysenate.sage.dao.base.BasicSqlQuery;
import gov.nysenate.sage.dao.base.SqlTable;

public enum ApiUserStatsQuery implements BasicSqlQuery {

    GET_REQUEST_COUNTS("SELECT ar.apiUserId, COUNT(DISTINCT ar.id) AS apiRequests, \n" +
            "                          COUNT(DISTINCT gr.id) AS geoRequests,\n" +
            "                          COUNT(DISTINCT dr.id) AS distRequests\n" +
            "FROM ${schema}." + SqlTable.API_REQUEST + " ar\n" +
            "LEFT JOIN ${schema}." + SqlTable.GEOCODE_REQUEST + " gr ON gr.apiRequestId = ar.id\n" +
            "LEFT JOIN ${schema}." + SqlTable.DISTRICT_REQUEST + " dr ON dr.apiRequestId = ar.id\n" +
            "WHERE ar.requestTime >= :from AND ar.requestTime <= :to\n" +
            "GROUP BY ar.apiUserId"),

    GET_METHOD_COUNTS("SELECT ar.apiUserId, s.name AS service, rt.name AS method, COUNT(*) AS requests\n" +
            "FROM ${schema}."+ SqlTable.API_REQUEST +" ar\n" +
            "LEFT JOIN ${schema}." + SqlTable.REQUESTTYPES + " rt ON ar.requestTypeId = rt.Id\n" +
            "LEFT JOIN ${schema}." + SqlTable.SERVICES + " s ON rt.serviceId = s.id\n" +
            "WHERE ar.requestTime >= :from AND ar.requestTime <= :to\n" +
            "GROUP BY ar.apiUserId, s.name, rt.name\n" +
            "ORDER BY ar.apiUserId, service, method"),

    ;

    private String sql;

    ApiUserStatsQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return this.sql;
    }
}
