package gov.nysenate.sage.dao.model.assembly;

import gov.nysenate.sage.dao.base.BasicSqlQuery;
import gov.nysenate.sage.dao.base.SqlTable;

public enum AssemblyQuery implements BasicSqlQuery { //${schema}." +SqlTable.PUBLIC_ASSEMBLY

    GET_ALL_ASSEMBLY_MEMBERS("SELECT * FROM ${schema}." +SqlTable.PUBLIC_ASSEMBLY),

    GET_ASSMEBLY_MEMBER_BY_DISTRICT("SELECT * FROM ${schema}." +SqlTable.PUBLIC_ASSEMBLY + " WHERE district = :district"),

    INSERT_ASSEMBLY_MEMBER("INSERT INTO ${schema}." +SqlTable.PUBLIC_ASSEMBLY + " (district, memberName, memberUrl) VALUES (:district,:memberName,:memberUrl)"),

    CLEAR_ASSEMBLY("DELETE FROM ${schema}." +SqlTable.PUBLIC_ASSEMBLY),

    DELETE_ASSEMBLY_DISTRICT("DELETE FROM ${schema}." +SqlTable.PUBLIC_ASSEMBLY + " WHERE district = :district")
    ;

    private String sql;

    AssemblyQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return this.sql;
    }
}
