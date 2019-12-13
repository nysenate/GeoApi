package gov.nysenate.sage.dao.data;

import gov.nysenate.sage.dao.base.BasicSqlQuery;
import gov.nysenate.sage.dao.base.SqlTable;

public enum DataGenQuery implements BasicSqlQuery {

    SELECT_SENATE_COUNTY_CODES("select name, id from ${schema}." +SqlTable.PUBLIC_COUNTY+";"),

    SELECT_TOWN_CODES("select name, abbrev from ${schema}." + SqlTable.DISTRICT_TOWN + ";"),

    SELECT_DISTRICT_ZIP("select zcta5ce10 from ${schema}." + SqlTable.DISTRICT_ZIP + ";"),

    SELECT_ADDRESSPOINT_AS_GEO_JSON("select zipcode, st_asgeojson(st_concavehull(st_collect(st_makepoint(${schema}.addresspoints_sam.longitude, ${schema}.addresspoints_sam.latitude)),0.5)) as geo from ${schema}." + SqlTable.ADDRESS_POINTS_SAM + " group by zipcode;"),

    SELECT_GEOCACHE_AS_GEO_JSON("select zip5, st_asgeojson(st_convexhull(st_collect(st_astext(${schema}.geocache.latlon)))) as geo from ${schema}. " + SqlTable.GEOCACHE + " group by zip5;"),

    //st_concavehull algorithm would result in precise polygon boundaries. Convexhull is used to avoid an error with intersection.
    SELECT_GEOCACHE_AS_GEO_JSON_CONCAVE("select zip5, st_asgeojson(st_concavehull(st_collect(st_astext(${schema}.geocache.latlon)),0.5)) as geo from ${schema}. " + SqlTable.GEOCACHE + " group by zip5;"),

    INSERT_MANUAL_DATAENTRY_GEOPOINT("INSERT INTO ${schema}." + SqlTable.MANUAL_DATAENTRY_GEOPOINTS + " (zipcode, type, lon, lat, source) " + "VALUES (?, ?, ?, ?, ?)"),

    SELECT_MANUAL_DATAENTRY_GEOPOINT("select zipcode, st_asgeojson(st_concavehull(st_collect(st_makepoint(${schema}.manual_dataentry_geopoints.lon::double precision,${schema}.manual_dataentry_geopoints.lat::double precision)),0.5)) as geo from ${schema}.manual_dataentry_geopoints group by zipcode;")
    ;

    private String sql;

    DataGenQuery(String sql) {
        this.sql = sql;

    }

    @Override
    public String getSql() {
        return this.sql;
    }
}
