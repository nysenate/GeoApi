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

    /**
     * ZIPS_DIRECTORY contains all target directory, almost all the files that are produced and read are in this directory.
     * ~Levidu
     */
    public static String ZIPS_DIRECTORY = "/data/geoapi_data/zips/";

    /**
     * Zipcodes that are saved from web crawling using jsoup are saved in two csv files
     * ~Levidu
     */
    public static String ZIPCODESTOGO_FILE = "zipcodestogo.csv";
    public static String ZIPCODES_FILE = "zipcodes.csv";

    /**
     * LAST_ZIPCODE_FILE: has all the zipcode (intersection from set ZIPCODESTOGO_FILE and ZIPCODES_FILE)
     * ~Levidu
     */
    public static String LAST_ZIPCODE_FILE = "final_list_zipcodes.csv";
    public static String ZIPS_IN_DISTRICTS_TABLE = "current_list_of_district_zipcodes.csv";
    public static String ZIPS_IN_FINAL_LIST_ZIPCODES = "final_zips.csv";
    public static String ZIPS_MISSING_FILE = "zipcodes_missing_from_db.csv";

    /**
     * USER_SPECIFIED_ZIP_SOURCE: CSV file that contains Zipcodes and the source to search (NYSGEO or geocache)
     * TSV_NYSGEO_GROUP_BY_ZIPCODES_FILE: TSV file that contains all geojson grouped by zipcode number in geocache db table. If deleted, the TSV
     * will be created by running the rubberband algorithm
     * TSV_GEOCACHE_GROUP_BY_ZIPCODES_FILE: TSV file that contains all geojson grouped by zipcode number in addresspoint_sam db table. If deleted, the TSV
     * will be created by running the rubberband algorithm.
     * ~Levidu
     */
    public static String USER_SPECIFIED_ZIP_SOURCE = "zip_source_tosearch.csv" ;
    public static String TSV_NYSGEO_GROUP_BY_ZIPCODES_FILE = "AsGeoJSON_addresspoint_ST_ConcaveHull.tsv";
    public static String TSV_GEOCACHE_GROUP_BY_ZIPCODES_FILE = "AsGeoJSON_geocache_ST_ConvexHull.tsv";

    /**
     * GEO_POINTS_SUPER_MANUAL: set of geo-points with zipcodes that are collected and ready to be converted to geoJSON files
     * ~Levidu
     */
    public static String GEO_POINTS_SUPER_MANUAL = "manual_dataentry_geopoints.csv";

    /**
     * Directories where all the geoJSON files are files are stored in.
     * For example, geoJSON files that are created using manual dataentry method will be stored in GEO_JSON_DIRECTORY_MANUAL
     * ~Levidu
     */
    public static String GEO_JSON_DIRECTORY_MANUAL = "/data/geoapi_data/zips/geojson/manual/";
    public static String GEO_JSON_DIRECTORY_NYSGEO = "/data/geoapi_data/zips/geojson/nysgeo/";
    public static String GEO_JSON_DIRECTORY_GEOCACHE = "/data/geoapi_data/zips/geojson/geocache/";






}
