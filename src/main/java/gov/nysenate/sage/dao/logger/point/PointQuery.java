package gov.nysenate.sage.dao.logger.point;

import gov.nysenate.sage.dao.base.BasicSqlQuery;
import gov.nysenate.sage.dao.base.SqlTable;

public enum PointQuery implements BasicSqlQuery {

    INSERT_POINT("INSERT INTO ${schema}." + SqlTable.POINT + " (latlon)\n" +
            "VALUES (ST_GeomFromText(:latlon)) \n" +
            "RETURNING id"),

    GET_POINT_ID("SELECT id FROM ${schema}." + SqlTable.POINT + "\n" +
            "WHERE latlon = ST_GeomFromText(:latlon)\n" +
            "LIMIT 1")
    ;

    private String sql;

    PointQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return this.sql;
    }
}
