package gov.nysenate.sage.dao.logger.apirequest;

import gov.nysenate.sage.dao.base.BasicSqlQuery;
import gov.nysenate.sage.dao.base.SqlTable;

public enum ApiRequestQuery implements BasicSqlQuery {
    INSERT_API_REQUEST(
            "INSERT INTO ${schema}." + SqlTable.API_REQUEST + "(ip_address, api_user_id, service, request, params)\n" +
            "VALUES (:ipAddress::INET, :apiUserId, :service, :request, :params)\n" +
            "RETURNING id"
    );

    private final String sql;

    ApiRequestQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return this.sql;
    }
}
