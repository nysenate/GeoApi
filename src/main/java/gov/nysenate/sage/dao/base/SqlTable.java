package gov.nysenate.sage.dao.base;

public enum SqlTable {
    /**
     * Geoapi Database table names
     */
    //Districts Schema
    DISTRICT_ASSEMBLY ("assembly"),
    DISTRICT_CONGRESSIONAL ("congressional"),
    DISTRICT_COUNTY ("county"),
    DISTRICT_ELECTION ("election"),
    DISTRICT_SCHOOL ("school"),
    DISTRICT_SENATE("senate"),
    DISTRICT_TOWN ("town"),
    DISTRICT_ZIP ("zip"),

    //Job Schema
    PROCESS ("process"),
    STATUS ("status"),
    USER ("user"),

    //Log Schema
    ADDRESS ("address"),
    API_REQUEST ("apirequest"),
    DEPLOYMENT ("deployment"),
    DISTRICT_REQUEST ("districtrequest"),
    DISTRICT_RESULT ("districtresult"),
    EXCEPTION ("exception"),
    GEOCODE_REQUEST("geocoderequest"),
    GEOCODE_RESULT("geocoderesult"),
    POINT ("point"),
    REQUESTTYPES ("requesttypes"),
    SERVICES ("services"),

    //Public Schema
    ADMIN ("admin"),
    API_USER ("apiuser"),
    PUBLIC_ASSEMBLY("assembly"),
    CITY_ZIP ("cityzip"),
    PUBLIC_CONGRESSIONAL ("congressional"),
    PUBLIC_COUNTY ("county"),
    PUBLIC_SENATE("senate"),
    SENATOR ("senator"),
    SPATIAL_REF_SYS ("spatial_ref_sys"),
    STREETFILE ("streetfile"),
    //This below SqlTable is used in createGeoJsonFromHardCodedSources service method ~Levidu
    MANUAL_DATAENTRY_GEOPOINTS("manual_dataentry_geopoints"),

    /**
     * Geocoder Database table names
     * Home of the geocache, and Tiger
     */
    //Cache Schema
    GEOCACHE ("geocache"),
    //Public Schema
    ADDRESS_POINTS_SAM ("addresspoints_sam")
    //For now, the tables used by Tiger will be left out. There are ~50 tables used only by TIGER

    ;
    /**
     * Rest of the Class
     */

    String tableName;

    SqlTable(String tableName) {
        this.tableName = tableName;
    }

    public String table(String schema) {
        return schema + "." + tableName;
    }

    @Override
    public String toString() {
        return tableName;
    }

    public String getTableName() {
        return tableName;
    }
}
