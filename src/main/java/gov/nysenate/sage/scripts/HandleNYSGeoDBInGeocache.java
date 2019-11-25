package gov.nysenate.sage.scripts;

import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.StreetAddress;
import gov.nysenate.sage.util.Config;
import gov.nysenate.sage.util.UrlRequest;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HandleNYSGeoDBInGeocache {

    private Config config;
    private QueryRunner tigerRun;
    private static final int limit = 2000;
    private static int offset = 0;
    private static final String geocache_table = "cache.geocache";
    private static final String method = "\'NYS Geo DB\'";

    public HandleNYSGeoDBInGeocache() {
        config = ApplicationFactory.getConfig();
        tigerRun = new QueryRunner(ApplicationFactory.getTigerDataSource());
    }

    public Config getConfig() {
        return config;
    }

    public QueryRunner getTigerRun() {
        return tigerRun;
    }

    public static int getLimit() {
        return limit;
    }

    public static int getOffset() {
        return offset;
    }

    public static String getGeocache_table() {
        return geocache_table;
    }

    public static String getMethod() {
        return method;
    }

    public static void main(String[] args) {
        LocalDateTime start;
        LocalDateTime end;
        /* Load up the configuration settings */
        if (!ApplicationFactory.bootstrap()) {
            System.err.println("Failed to configure application");
            System.exit(-1);
        }

        //Check for offset specification
        try {
            if (args.length > 0 && args.length < 2) {
                offset = Integer.parseInt(args[0]);
            }
            System.out.println("Handle NYS Geo DB data supplied offset: " + offset);
        } catch (Exception e) {
            System.err.println("Check supplied arguments. The only argument should be an integer, " +
                    "representing the starting point of the data offset");
            System.exit(-1);
        }

        start = LocalDateTime.now();

        HandleNYSGeoDBInGeocache nysGeoDBInGeocache = new HandleNYSGeoDBInGeocache();

        //Sql for finding what needs to be regeocached
        String NYS_GEO_TOTAL_COUNT_SQL = "select count(*)\n" +
                "from " + getGeocache_table() + "\n" +
                "where method = " + getMethod() + ";";

        String NYS_GEO_DB_BATCH_SQL = "select *\n" +
                "from " + getGeocache_table() + "\n" +
                "where method = " + getMethod() + "\n" +
                "offset ?\n" +
                "limit ?;";

        HttpClient httpClient = HttpClientBuilder.create().build();
        //Create count for showing the requests made to SAGE
        int regeocacheQueries = 0;
        //Get the total count to find the limit of the loops
        try {
            //Get total number of addresses that will be used to update our geocache
            int total = nysGeoDBInGeocache.getTigerRun().query(NYS_GEO_TOTAL_COUNT_SQL, new ResultSetHandler<Integer>() {
                @Override
                public Integer handle(ResultSet rs) throws SQLException {
                    rs.next();
                    return rs.getInt("count");
                }
            });
            System.out.println("Total number of records currently cached with the method 'NYS Geo DB': " + total);

            //Where the magic happens
            while (total > offset) {
                //Get batch of 2000
                List<StreetAddress> nysGeoDBAddresses = nysGeoDBInGeocache.getTigerRun().query(NYS_GEO_DB_BATCH_SQL,
                        new ResultSetHandler<List<StreetAddress>>() {
                            @Override
                            public List<StreetAddress> handle(ResultSet rs) throws SQLException {
                                List<StreetAddress> addresses = new ArrayList<>();
                                while (rs.next()) {
                                    StreetAddress sa = new StreetAddress();
                                    sa.setBldgNum(rs.getInt("bldgnum"));
                                    sa.setPreDir(rs.getString("predir"));
                                    sa.setStreetName(WordUtils.capitalizeFully(rs.getString("street")));
                                    sa.setStreetType(WordUtils.capitalizeFully(rs.getString("streettype")));
                                    sa.setPostDir(rs.getString("postdir"));
                                    sa.setLocation(WordUtils.capitalizeFully(rs.getString("location")));
                                    sa.setState(rs.getString("state"));
                                    sa.setZip5(rs.getString("zip5"));
                                    sa.setZip4(rs.getString("zip4"));
                                    addresses.add(sa);
                                }
                                return addresses;
                            }
                        }, offset, limit);

                //Let the admin know about progress made & increment offset
                System.out.println("At offset: " + offset);
                offset = limit + offset;

                if (!nysGeoDBAddresses.isEmpty()) {
                    //Query SAGE with the nysgeo webservice specified
                    for (StreetAddress nysGeoStreetAddress : nysGeoDBAddresses) {

                        //Build URL
                        Address nysgeoAddress = nysGeoStreetAddress.toAddress();
                        String regeocacheUrl = nysGeoDBInGeocache.config.getValue("base.url") +
                                "/api/v2/geo/geocode?addr1=%s&addr2=%s&city=%s&state=%s&zip5=%s&provider=nysgeo";
                        regeocacheUrl = String.format(regeocacheUrl, nysgeoAddress.getAddr1(), nysgeoAddress.getAddr2(),
                                nysgeoAddress.getCity(), nysgeoAddress.getState(), nysgeoAddress.getZip5());
                        regeocacheUrl = regeocacheUrl.replaceAll(" ", "%20");
                        regeocacheUrl = StringUtils.deleteWhitespace(regeocacheUrl);
                        regeocacheUrl = regeocacheUrl.replaceAll("`", "");
                        regeocacheUrl = regeocacheUrl.replaceAll("#", "");
                        regeocacheUrl = regeocacheUrl.replaceAll("\\\\", "");

                        //Execute URL
                        try {
                            HttpGet request = new HttpGet(regeocacheUrl);
                            HttpResponse response = httpClient.execute(request);
                            UrlRequest.convertStreamToString(response.getEntity().getContent());
                            regeocacheQueries++;
                        } catch (Exception e) {
                            //Alert Admin to failures
                            System.err.println("Failed to contact SAGE with the url: " + regeocacheUrl);
                            continue;
                        }
                    }

                }
            }
            end = LocalDateTime.now();

            //Let the admin know about the work that has been done.
            System.out.println("Queries to SAGE with the NYS GEO Webservice specified: " + regeocacheQueries);
            System.out.println("The script started at " + start + " and ended at " + end);
        }
        catch (Exception e) {
            System.err.println("Error refreshing the geocoder database:" + e);
        }
    }
}
