package gov.nysenate.sage.dao.stats.deployment;

import gov.nysenate.sage.dao.base.BasicSqlQuery;
import gov.nysenate.sage.dao.base.SqlTable;

public enum DeploymentStatsQuery  implements BasicSqlQuery {
    // TODO: The last column is not filled in database
    SELECT_DEPLOY_STATS("SELECT id, deploy_time, api_requests_since \n" +
            "FROM ${schema}." + SqlTable.DEPLOYMENT + " \n" +
            "ORDER BY deploy_time ASC");

    private final String sql;

    DeploymentStatsQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return this.sql;
    }
}
