package gov.nysenate.sage.dao.data;

import gov.nysenate.sage.dao.base.BasicSqlQuery;
import gov.nysenate.sage.dao.base.SqlTable;

public enum RegeocacheQuery implements BasicSqlQuery {

    NYS_BATCH_SQL("select d.addresslabel, d.citytownname, d.state, d.zipcode, d.latitude, d.longitude, d.pointtype\n" +
            "from ${schema}." + SqlTable.ADDRESS_POINTS_SAM + " d\n" +
            "order by objectid asc\n" +
            "limit :limit OFFSET :offset"),

    NYS_COUNT_SQL("select count(*) from ${schema}." + SqlTable.ADDRESS_POINTS_SAM),

    GEOCACHE_SELECT("SELECT gc.method \n" +
            "FROM ${schema}." + SqlTable.GEOCACHE + " AS gc \n" +
            "WHERE gc.bldgnum = :bldgnum \n" +
            "AND gc.predir = :predir \n" +
            "AND gc.street = :street \n" +
            "AND gc.postdir = :postdir \n" +
            "AND gc.streetType = :streettype \n" +
            "AND gc.zip5 = :zip5 \n" +
            "AND gc.location = :location \n"),

    INSERT_GEOCACHE("INSERT INTO ${schema}." + SqlTable.GEOCACHE + " (bldgnum, predir, street, streettype, postdir, location, state, zip5, " +
            "latlon, method, quality, zip4) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ST_GeomFromText(?), ?, ?, ?)"),

    UPDATE_GEOCACHE("update ${schema}." + SqlTable.GEOCACHE + "\n" +
            "set latlon = ST_GeomFromText(?), method = ?, quality = ?, zip4 = ?, updated = now()\n" +
            "where bldgnum = ?  and street = ? and streettype = ? and predir = ? and postdir = ?;"),

    SELECT_ZIPS("select zcta5ce10 from ${schema}." + SqlTable.DISTRICT_ZIP + ";")
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
