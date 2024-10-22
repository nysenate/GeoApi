package gov.nysenate.sage.dao.stats.deployment;

import gov.nysenate.sage.dao.base.BasicSqlQuery;
import gov.nysenate.sage.dao.base.SqlTable;

public enum DeploymentStatsQuery  implements BasicSqlQuery {
    SELECT_DEPLOY_STATS("SELECT id, deployed, refId AS deploymentRef, deployTime, apiRequestsSince \n" +
            "FROM ${schema}." + SqlTable.DEPLOYMENT + " \n" +
            "ORDER BY deploytime ASC");

    private final String sql;

    DeploymentStatsQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return this.sql;
    }
}
