package gov.nysenate.sage.scripts;

import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedStreetAddress;
import gov.nysenate.sage.model.address.NYSGeoAddress;
import gov.nysenate.sage.model.address.StreetAddress;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;
import gov.nysenate.sage.util.Config;
import gov.nysenate.sage.util.StreetAddressParser;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class NYSRefreshGeocache {

    private Config config;
    private QueryRunner tigerRun;
    private static final int limit = 2000;
    private static int offset = 0;
    private static final String table = "public.addresspoints_sam";

    public NYSRefreshGeocache() {
        config = ApplicationFactory.getConfig();
        tigerRun = new QueryRunner(ApplicationFactory.getTigerDataSource());
    }

    public String convertStreamToString(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
        return sb.toString();
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

    public static String getTable() {
        return table;
    }

    private void update(StreetAddress nysStreetAddress,
                        Geocode nysGeocode, String UPDATE_GEOCACHE,
                        String geocachedStreetAddressProvider,
                        NYSGeoAddress nysGeoAddress) throws SQLException {
        if (!geocachedStreetAddressProvider.equals("GoogleDao") && nysGeoAddress.getPointtype() == 1) {
            this.getTigerRun().update(UPDATE_GEOCACHE,
                    "POINT(" + nysGeocode.getLon() + " " + nysGeocode.getLat() + ")",
                    nysGeocode.getMethod(), nysGeocode.getQuality().name(), nysStreetAddress.getZip4(),
                    nysStreetAddress.getBldgNum(), nysStreetAddress.getStreetName(),
                    nysStreetAddress.getStreetType(), nysStreetAddress.getPreDir(),
                    nysStreetAddress.getPostDir(),
                    nysStreetAddress.getLocation(),
                    nysStreetAddress.getZip5()
            );
        }
    }


    private void insert(StreetAddress nysStreetAddress, Geocode nysGeocode, String INSERT_GEOCACHE) throws SQLException {
        this.getTigerRun().update(INSERT_GEOCACHE, Integer.valueOf(nysStreetAddress.getBldgNum()),
                nysStreetAddress.getPreDir(), nysStreetAddress.getStreetName(), nysStreetAddress.getStreetType(),
                nysStreetAddress.getPostDir(), nysStreetAddress.getLocation(),
                nysStreetAddress.getState(), nysStreetAddress.getZip5(),
                "POINT(" + nysGeocode.getLon() + " " + nysGeocode.getLat() + ")",
                nysGeocode.getMethod(), nysGeocode.getQuality().name(), nysStreetAddress.getZip4()
        );
    }

    public static void main(String[] args) {
        /* Load up the configuration settings */
        if (!ApplicationFactory.bootstrap()) {
            System.err.println("Failed to configure application");
            System.exit(-1);
        }

        try {
            if (args.length > 0 && args.length < 2) {
                offset = Integer.parseInt(args[0]);
            }
            System.out.println("NYS Refresh Geocache input offset: " + offset);
        } catch (Exception e) {
            System.err.println(
                    "Check supplied arguments. The only argument should be an int");
            System.exit(-1);
        }

        NYSRefreshGeocache nysRefreshGeocache = new NYSRefreshGeocache();

        String NYS_BATCH_SQL = "select d.addresslabel, d.citytownname, d.state, d.zipcode, d.latitude, d.longitude, d.pointtype\n" +
                "from public.addresspoints_sam d\n" +
                "order by objectid asc\n" +
                "limit ? OFFSET ?";

        String NYS_COUNT_SQL = "select count(*) from public.addresspoints_sam";

        String GEOCACHE_SELECT =
                "SELECT * \n" +
                        "FROM cache.geocache AS gc \n" +
                        "WHERE gc.bldgnum = ? \n" +
                        "AND gc.predir = ? \n" +
                        "AND gc.street = ? \n" +
                        "AND gc.postdir = ? \n" +
                        "AND gc.streetType = ? \n" +
                        "AND gc.zip5 = ? \n" +
                        "AND gc.location = ? \n";

        String INSERT_GEOCACHE =
                "INSERT INTO cache.geocache (bldgnum, predir, street, streettype, postdir, location, state, zip5, " +
                        "latlon, method, quality, zip4) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ST_GeomFromText(?), ?, ?, ?)";

        String UPDATE_GEOCACHE = "update cache.geocache\n" +
                "set latlon = ST_GeomFromText(?), method = ?, quality = ?, zip4 = ?, updated = now()\n" +
                "where bldgnum = ?  and street = ? and streettype = ? and predir = ? and postdir = ? and location = ? and zip5 = ?;";

        BeanListHandler<NYSGeoAddress> nysGeoAddressBeanListHandler
                = new BeanListHandler<>(NYSGeoAddress.class);

        int updatedRecordsCount = 0;

        HttpClient httpClient = HttpClientBuilder.create().build();
        /*
        Execute SQL
         */
        try {
            //Get total number of addresses that will be used to update our geocache
            int total = nysRefreshGeocache.getTigerRun().query(NYS_COUNT_SQL, new ResultSetHandler<Integer>() {
                @Override
                public Integer handle(ResultSet rs) throws SQLException {
                    rs.next();
                    return rs.getInt("count");
                }
            });
            System.out.println("NYS Geo total record count: " + total);

            //start from 0 and loop until the total number in batches of 2000
            while (total > offset) {
                //Get batch of 2000
                List<NYSGeoAddress> nysGeoAddresses = nysRefreshGeocache.getTigerRun().query(NYS_BATCH_SQL,
                        nysGeoAddressBeanListHandler, limit, offset);
                System.out.println("At offset: " + offset);
                offset = limit + offset;

                //Handle batch
                for (NYSGeoAddress nysGeoAddress : nysGeoAddresses) {
                    //Convert NYSGeoAddress to an Address Object
                    Address nysAddress = nysGeoAddress.toAddress();

                    //Extract NYSGeoAddress coordinates to form a geocode
                    Geocode nysGeocode = nysGeoAddress.toGeocode();

                    //Convert NYSGeoAddress to a Street address like our cache
                    StreetAddress nysStreetAddress = nysGeoAddress.toStreetAddress();

                    //Run new Address through USPS
                    String url = nysRefreshGeocache.config.getValue("usps.ams.api.url") + "validate?detail=true&addr1=%s&addr2=&city=%s&state=%s&zip5=%s&zip4=";
                    url = String.format(url, nysAddress.getAddr1(), nysAddress.getCity(), nysAddress.getState(), nysAddress.getZip5());
                    url = url.replaceAll(" ", "%20");
                    url = StringUtils.deleteWhitespace(url);
                    url = url.replaceAll("`", "");
                    url = url.replaceAll("#", "");

                    try {
                        HttpGet request = new HttpGet(url);
                        HttpResponse response = httpClient.execute(request);

                        JSONObject uspsJson = new JSONObject(nysRefreshGeocache.convertStreamToString(response.getEntity().getContent()));
                        if (uspsJson.getBoolean("validated")) {
                            JSONObject uspsAddressJson = uspsJson.getJSONObject("address");
                            nysStreetAddress = StreetAddressParser.parseAddress(new Address(
                                    uspsAddressJson.getString("addr1"), uspsAddressJson.getString("addr2"),
                                    uspsAddressJson.getString("city"), uspsAddressJson.getString("state"),
                                    uspsAddressJson.getString("zip5"), uspsAddressJson.getString("zip4")));
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to make Http request because of exception" + e.getMessage());
                        System.err.println("Failed address was: " + nysAddress.toString());
                        continue; //only put it in the database if USPS has data on it / corrected the address
                    }

                    //Determine if address exits in our Geocache by getting the method of its results (GoogleDao, YahooDao, etc)
                    GeocodedStreetAddress geocachedStreetAddress = nysRefreshGeocache.getTigerRun().query(GEOCACHE_SELECT,
                    new ResultSetHandler<GeocodedStreetAddress>() {
                                @Override
                                public GeocodedStreetAddress handle(ResultSet rs) throws SQLException {
                                    if (rs != null && rs.next()) {
                                        Geocode gc = new Geocode();
                                        gc.setMethod(rs.getString("method"));
                                        gc.setQuality(GeocodeQuality.valueOf(rs.getString("quality").toUpperCase()));
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
                                        return new GeocodedStreetAddress(sa, gc);
                                    }
                                    return null;
                                }
                            }, nysStreetAddress.getBldgNum(),
                            nysStreetAddress.getPreDir(),
                            nysStreetAddress.getStreetName(),
                            nysStreetAddress.getPostDir(),
                            nysStreetAddress.getStreetType(),
                            nysStreetAddress.getZip5(),
                            nysStreetAddress.getLocation());

                    String geocachedStreetAddressProvider = "";
                    if (geocachedStreetAddress != null && geocachedStreetAddress.getStreetAddress().equals(nysStreetAddress) ) {
                        geocachedStreetAddressProvider = geocachedStreetAddress.getGeocode().getMethod();
                        //update only if its not google. Address was matched so it should insert fine
                        if (!geocachedStreetAddressProvider.equals("GoogleDao") && nysGeoAddress.getPointtype() == 1) {
                            try {
                                nysRefreshGeocache.update(nysStreetAddress, nysGeocode,
                                        UPDATE_GEOCACHE, geocachedStreetAddressProvider, nysGeoAddress);
                                updatedRecordsCount++;
                            } catch (SQLException e) {
                                //should not happen beacause it was verified
                                System.out.println("Update Failed for " + nysStreetAddress.toString());
                            }
                        }
                    }
                    //If the geocacheStreetAddressProvider is empty, we don't have the address cached, so insert the address
                    else if (StringUtils.isEmpty(geocachedStreetAddressProvider)) {
                        //insert
                        try {
                            nysRefreshGeocache.insert(nysStreetAddress, nysGeocode, INSERT_GEOCACHE);
                            updatedRecordsCount++;
                        } catch (SQLException e) {
                            System.out.println("Insert Failed for " + nysStreetAddress.toString() );
                        }
                    }
                }
            }
            try {
                ((CloseableHttpClient) httpClient).close();
                System.out.println(updatedRecordsCount + " were updated");
            } catch (IOException e) {
                System.err.println("Failed to close http connection \n" + e);
            }
            ApplicationFactory.close();
            System.exit(0);
        } catch (SQLException ex) {
            System.err.println("Error retrieving addresses from geocache \n" + ex);
        }
    }
}
