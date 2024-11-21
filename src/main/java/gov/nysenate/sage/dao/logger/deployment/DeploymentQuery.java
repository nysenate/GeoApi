package gov.nysenate.sage.dao.logger.deployment;

import gov.nysenate.sage.dao.base.BasicSqlQuery;
import gov.nysenate.sage.dao.base.SqlTable;

public enum DeploymentQuery implements BasicSqlQuery {
    INSERT_DEPLOYMENT("INSERT INTO ${schema}." + SqlTable.DEPLOYMENT + " DEFAULT VALUES");

    private final String sql;

    DeploymentQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return this.sql;
    }
}
