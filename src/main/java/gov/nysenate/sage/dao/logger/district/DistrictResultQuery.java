package gov.nysenate.sage.dao.logger.district;

import gov.nysenate.sage.dao.base.BasicSqlQuery;
import gov.nysenate.sage.dao.base.SqlTable;

public enum DistrictResultQuery implements BasicSqlQuery {

    INSERT_RESULT("INSERT INTO ${schema}. "+ SqlTable.DISTRICT_RESULT + "(districtrequestid, assigned, status, senatecode, assemblycode," +
            "congressionalcode, countycode, town_code, school_code, matchLevel, resulttime) \n" +
            "VALUES (:districtrequestid, :assigned, :status, :senatecode, :assemblycode, ;congressionalcode, ;countycode, ;town_code, :school_code, :matchLevel, :resulttime) \n" +
            "RETURNING id")
    ;

    private String sql;

    DistrictResultQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return this.sql;
    }
}
