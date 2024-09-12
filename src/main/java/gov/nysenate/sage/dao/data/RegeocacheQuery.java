package gov.nysenate.sage.dao.data;

import gov.nysenate.sage.dao.base.BasicSqlQuery;
import gov.nysenate.sage.dao.base.SqlTable;

public enum RegeocacheQuery implements BasicSqlQuery {

    MASS_GEOCACHE_COUNT(  "select count(*) from ${schema}." + SqlTable.GEOCACHE),

    MASS_GEOCACHE_SELECT( "select * from ${schema}." + SqlTable.GEOCACHE),

    MASS_GEOCACHE_WHERE_METHOD(" where method ilike :method "),

    MASS_GEOCACHE_WHERE_ZIP(" where zip5 = :zipcode "),

    MASS_GEOCACHE_WHERE_LOCATION(" where location ilike :location "),

    MASS_GEOCACHE_WHERE_QUALITY(" where quality = :quality "),

    MASS_GEOCACHE_AND_METHOD(" and method = :method "),

    MASS_GEOCACHE_AND_ZIP(" and zip5 = :zipcode "),

    MASS_GEOCACHE_AND_LOCATION(" and location ilike :location "),

    MASS_GEOCACHE_AND_QUALITY(" and quality = :quality "),

    MASS_GEOCACHE_LIMIT_OFFSET (" limit :limit offset :offset"),

    MASS_GEOCACHE_ORDER_BY(" ORDER By id asc"),

    NYS_BATCH_SQL("select d.addresslabel, d.citytownname, d.state, d.zipcode, d.latitude, d.longitude, d.pointtype\n" +
            "from ${schema}." + SqlTable.ADDRESS_POINTS_SAM + " d\n" +
            "order by objectid asc\n" +
            "limit :limit OFFSET :offset"),

    NYS_COUNT_SQL("select count(*) from ${schema}." + SqlTable.ADDRESS_POINTS_SAM),

    GEOCACHE_SELECT("SELECT * \n" +
            "FROM ${schema}." + SqlTable.GEOCACHE + " AS gc \n" +
            "WHERE gc.bldgnum = :bldgnum \n" +
            "AND gc.street = :street \n" +
            "AND gc.zip5 = :zip5 \n" +
            "AND gc.location = :location \n"),

    INSERT_GEOCACHE("INSERT INTO ${schema}." + SqlTable.GEOCACHE + " (bldgnum, street, location, zip5, " +
            "latlon, method, quality, zip4) " +
            "VALUES (?, ?, ?, ?, ST_GeomFromText(?), ?, ?, ?)"),

    UPDATE_GEOCACHE("update ${schema}." + SqlTable.GEOCACHE + "\n" +
            "set latlon = ST_GeomFromText(?), method = ?, quality = ?, zip4 = ?, updated = now()\n" +
            "where bldgnum = ?  and street = ? and zip5 = ? and location = ?;"),

    SELECT_ZIPS("select zip_code from ${schema}." + SqlTable.DISTRICT_ZIP + ";"),

    METHOD_TOTAL_COUNT_SQL("select count(*) \n" +
            "from ${schema}." + SqlTable.GEOCACHE + "\n" +
            "where method = :method;"),

    METHOD_BATCH_SQL("select * \n" +
            "from ${schema}." + SqlTable.GEOCACHE + "\n" +
            "where method = :method \n" +
            "limit :limit offset :offset;"),

    ;

    private String sql;

    RegeocacheQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return this.sql;
    }
}
