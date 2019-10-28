package gov.nysenate.sage.dao.data;

import gov.nysenate.sage.dao.base.BasicSqlQuery;
import gov.nysenate.sage.dao.base.SqlTable;

public enum DataGenQuery implements BasicSqlQuery {

    SELECT_SENATE_COUNTY_CODES("select name, id from ${schema}." +SqlTable.PUBLIC_COUNTY+";"),

    SELECT_TOWN_CODES("select name, abbrev from ${schema}." + SqlTable.DISTRICT_TOWN + ";"),

    SELECT_DISTRICT_ZIP("select zcta5ce10 from ${schema}." + SqlTable.DISTRICT_ZIP + ";"),

    SELECT_ASGEOJSON("SELECT ST_AsGeoJSON(ST_ConcaveHull(ST_collect(ST_MakePoint(public.addresspoints_sam.longitude,public.addresspoints_sam.latitude)),0.5)) as points FROM public.addresspoints_sam WHERE zipcode='11249';")

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
