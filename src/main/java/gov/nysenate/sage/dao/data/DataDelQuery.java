package gov.nysenate.sage.dao.data;

import gov.nysenate.sage.dao.base.BasicSqlQuery;
import gov.nysenate.sage.dao.base.SqlTable;

public enum DataDelQuery implements BasicSqlQuery {
    ZIP_COUNT_SQL("select count(distinct zip5) from ${schema}." + SqlTable.GEOCACHE + ";"),

    ZIP_BATCH_SQL("select distinct zip5 from ${schema}." + SqlTable.GEOCACHE + " limit ? offset ?;"),

    DELETE_ZIP_SQL("Delete from ${schema}." + SqlTable.GEOCACHE + " where zip5 = ?;"),

    STATE_COUNT_SQL("select count(distinct state) from ${schema}." + SqlTable.GEOCACHE + " ;"),

    STATE_BATCH_SQL("select distinct state from ${schema}." + SqlTable.GEOCACHE + " limit ? offset ?;"),

    DELETE_STATE_SQL("Delete from ${schema}." + SqlTable.GEOCACHE + " where state = ?;");

    private final String sql;

    DataDelQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return this.sql;
    }

}
