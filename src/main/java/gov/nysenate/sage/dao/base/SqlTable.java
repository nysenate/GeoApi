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
    API_REQUEST ("api_request"),
    DEPLOYMENT ("deployment"),
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
    GEOCACHE ("geocache");

    private final String tableName;

    SqlTable(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public String toString() {
        return tableName;
    }
}
