package gov.nysenate.sage.dao.logger.apirequest;

import gov.nysenate.sage.dao.base.BasicSqlQuery;
import gov.nysenate.sage.dao.base.SqlTable;

public enum ApiRequestQuery implements BasicSqlQuery {

    INSERT_API_REQUEST("INSERT INTO ${schema}." + SqlTable.API_REQUEST + "(ipAddress, apiUserId, version, requestTypeId, requestTime, isBatch) \n" +
            "SELECT :ipAddress::inet, :apiUserId, :version, rt.id, :requestTime, :isBatch \n" +
            "FROM log.requestTypes AS rt \n" +
            "LEFT JOIN log.services ser ON rt.serviceId = ser.id \n" +
            "WHERE rt.name = :requestTypeName AND ser.name = :serviceName\n" +
            "RETURNING id"),

    GET_API_REQUEST("SELECT ${schema}."+ SqlTable.API_REQUEST + ".id AS requestId, ipAddress, version, serv.name AS service, rt.name AS request, isBatch, requestTime, \n" +
            "au.id AS apiUserId, au.name AS apiUserName, au.apiKey AS apiKey, au.description AS apiUserDesc " +
            "FROM ${schema}." + SqlTable.API_REQUEST + "\n" +
            "LEFT JOIN " + "public.apiUser au ON apiUserId = au.id \n" +
            "LEFT JOIN ${schema}."  + "requestTypes rt ON requestTypeId = rt.id \n" +
            "LEFT JOIN ${schema}." + "services serv ON rt.serviceId = serv.id \n" +
            "WHERE ${schema}." + SqlTable.API_REQUEST + ".id = :apiRequestId"),

    GET_RANGE_API_REQUESTS("SELECT log.apiRequest.id AS requestId, ipAddress, version, serv.name AS service, rt.name AS request, isBatch, requestTime, \n" +
            "       au.id AS apiUserId, au.name AS apiUserName, au.apiKey AS apiKey, au.description AS apiUserDesc \n" +
            "FROM ${schema}." + "." + SqlTable.API_REQUEST + "\n" +
            "LEFT JOIN " + "public.apiUser au ON apiUserId = au.id \n" +
            "LEFT JOIN ${schema}.requestTypes rt ON requestTypeId = rt.id \n" +
            "LEFT JOIN ${schema}.services serv ON rt.serviceId = serv.id \n" +
            "WHERE requestTime >= :from AND requestTime <= :to \n" +
            "AND CASE WHEN :service != '' THEN rt.name = :service ELSE true END\n" +
            "AND CASE WHEN :method != '' THEN serv.name = :method, ELSE true END\n"),

    ;

    private String sql;

    ApiRequestQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return this.sql;
    }
}
