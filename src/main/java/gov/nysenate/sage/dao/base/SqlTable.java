package gov.nysenate.sage.dao.base;

public enum SqlTable {
    /**
     * Geoapi Database table names
     */
    //Districts Schema
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
    POST_OFFICE("post_office"),
    PUBLIC_CONGRESSIONAL ("congressional"),
    PUBLIC_COUNTY ("county"),
    PUBLIC_SENATE("senate"),
    SENATOR ("senator"),
    // TODO: use
    STREETFILE ("streetfile"),

    /**
     * Geocoder Database table names
     * Home of the geocache, and Tiger
     */
    //Cache Schema
    GEOCACHE ("geocache"),
    //Public Schema
    ADDRESS_POINTS_SAM ("addresspoints_sam");
    // TODO: cleanup tiget tables

    private final String tableName;

    SqlTable(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public String toString() {
        return tableName;
    }
}
