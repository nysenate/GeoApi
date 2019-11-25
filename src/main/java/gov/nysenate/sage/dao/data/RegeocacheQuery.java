package gov.nysenate.sage.dao.data;

import gov.nysenate.sage.dao.base.BasicSqlQuery;
import gov.nysenate.sage.dao.base.SqlTable;

public enum RegeocacheQuery implements BasicSqlQuery {

    NYS_BATCH_SQL("select d.addresslabel, d.citytownname, d.state, d.zipcode, d.latitude, d.longitude, d.pointtype\n" +
            "from ${schema}." + SqlTable.ADDRESS_POINTS_SAM + " d\n" +
            "order by objectid asc\n" +
            "limit :limit OFFSET :offset"),

    NYS_COUNT_SQL("select count(*) from ${schema}." + SqlTable.ADDRESS_POINTS_SAM),

    GEOCACHE_SELECT("SELECT * \n" +
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
            "where bldgnum = ?  and street = ? and streettype = ? and predir = ? and postdir = ? and zip5 = ? and location = ?;"),

    SELECT_ZIPS("select zcta5ce10 from ${schema}." + SqlTable.DISTRICT_ZIP + ";"),

    DUP_TOTAL_COUNT_SQL("SELECT count(*)\n" +
            "FROM " + SqlTable.ADDRESS_POINTS_SAM + " x\n" +
            "         JOIN (SELECT t.addresslabel\n" +
            "               FROM addresspoints_sam t\n" +
            "               GROUP BY t.addresslabel\n" +
            "               HAVING COUNT(t.addresslabel) > 1) y ON y.addresslabel = x.addresslabel;"),

    DUP_BATCH_SQL("SELECT  x.addresslabel, x.citytownname, x.state, x.zipcode, x.latitude, x.longitude, x.pointtype\n" +
            "FROM " + SqlTable.ADDRESS_POINTS_SAM + " x\n" +
            "         JOIN (SELECT t.addresslabel\n" +
            "               FROM addresspoints_sam t\n" +
            "               GROUP BY t.addresslabel\n" +
            "               HAVING COUNT(t.addresslabel) > 1) y ON y.addresslabel = x.addresslabel\n" +
            "limit :limit\n" +
            "offset :offset;"),

    METHOD_TOTAL_COUNT_SQL("select count(*) \n" +
            "from ${schema}." + SqlTable.GEOCACHE + "\n" +
            "where method = :method;"),

    METHOD_BATCH_SQL("select * \n" +
            "from ${schema}." + SqlTable.GEOCACHE + "\n" +
            "where method = :method \n" +
            "limit :limit offset :offset;")


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
