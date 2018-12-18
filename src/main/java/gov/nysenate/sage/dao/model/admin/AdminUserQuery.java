package gov.nysenate.sage.dao.model.admin;

import gov.nysenate.sage.dao.base.BasicSqlQuery;
import gov.nysenate.sage.dao.base.SqlTable;

public enum AdminUserQuery implements BasicSqlQuery { //${schema}.
    GET_ADMIN("SELECT * FROM ${schema}." + SqlTable.ADMIN + "\n" +
            "WHERE username = :username"),

    INSERT_ADMIN( "INSERT INTO ${schema}." + SqlTable.ADMIN + " (username, password) VALUES (:username,:password) RETURNING id;"),

            ;

    private String sql;

    AdminUserQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return this.sql;
    }
}
