package gov.nysenate.sage.util.controller;

public class ConstantUtil {

    public static String AUTH_ATTR = "authenticated";

    public static String ADMIN_USERNAME_ATTR = "adminUserName";
    public static String ADMIN_REQUEST_ATTR = "adminRequest";

    public static String ADMIN_LOGIN_PATH = "/admin/login";
    public static String ADMIN_LOGIN_JSP = "/WEB-INF/views/adminlogin.jsp";
    public static String ADMIN_MAIN_PATH = "/admin";
    public static String ADMIN_MAIN_JSP = "/WEB-INF/views/adminmain.jsp";

    public static String JOB_USER_ATTR = "jobuser";
    public static String JOB_REQUEST_ATTR = "jobrequest";

    public static final String REST_PATH = "/api/v2/";

    public static final String ADMIN_REST_PATH = "/admin";

    public static String MAPS_JSP = "/WEB-INF/views/maps.jsp";

    public static String JOB_MAIN_JSP = "/WEB-INF/views/jobmain.jsp";
    public static String JOB_LOGIN_JSP = "/WEB-INF/views/joblogin.jsp";
    public static String DOWNLOAD_BASE_URL = "/job/download/";

    public static String COUNTY_FILE = "senate_counties.txt";

    public static String TOWN_FILE = "towns.txt";

    public static String STREETFINDER_DIRECTORY = "/data/geoapi_data/street_finder/";


    public static String ZIPS_DIRECTORY = "/data/geoapi_data/zips/";

    public static String ZIPCODESTOGO_FILE = "zipcodestogo.csv";
    public static String ZIPCODES_FILE = "zipcodes.csv";
    public static String LAST_ZIPCODE_FILE = "final_list_zipcodes.csv";

    public static String ZIPS_IN_DISTRICTS_TABLE = "current_list_of_district_zipcodes.csv";
    public static String ZIPS_IN_FINAL_LIST_ZIPCODES = "final_zips.csv";
    public static String ZIPS_MISSING_FILE = "zipcodes_missing_from_db.csv";

    public static String USER_SPECIFIED_ZIP_SOURCE = "zip_source_tosearch.csv" ;


    public static String TSV_NYSGEO_GROUP_BY_ZIPCODES_FILE = "AsGeoJSON_addresspoint_ST_ConcaveHull.tsv";
    public static String TSV_GEOCACHE_GROUP_BY_ZIPCODES_FILE = "AsGeoJSON_geocache_ST_ConvexHull.tsv";


    public static String GEO_POINTS_SUPER_MANUAL = "manual_dataentry_geopoints.csv";
    public static String GEO_JSON_DIRECTORY_NYSGEO = "/data/geoapi_data/zips/geojson/nysgeo/";
    public static String GEO_JSON_DIRECTORY_GEOCACHE = "/data/geoapi_data/zips/geojson/geocache/";


    public static String GEO_JSON_DIRECTORY_MANUAL = "/data/geoapi_data/zips/geojson/manual/";





}
