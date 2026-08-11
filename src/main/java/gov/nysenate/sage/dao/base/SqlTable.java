package gov.nysenate.sage.dao.base;

public enum SqlTable {
    /**
     * Geoapi Database table names
     */
    //Districts Schema
    TYPE_INFO("type_info"),

    //Job Schema
    PROCESS ("process"),
    STATUS ("status"),
    USER ("user"),

    //Log Schema
    API_REQUEST ("api_request"),
    DEPLOYMENT ("deployment"),
    GEOCODE_STATS("geocode_stats"),

    //Public Schema
    ADMIN ("admin"),
    API_USER ("apiuser"),
    POST_OFFICE("post_office"),
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
