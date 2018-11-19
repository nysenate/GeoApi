package gov.nysenate.sage.scripts;

import gov.nysenate.sage.config.DatabaseConfig;
import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.NYSGeoAddress;
import gov.nysenate.sage.model.address.StreetAddress;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.util.StreetAddressParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static gov.nysenate.sage.scripts.BaseScript.getCommandLine;

@Component
public class NYSRefreshGeocache {


    @Autowired
    Environment env;
    @Autowired
    DatabaseConfig databaseConfig;

    private static final int limit = 2000;
    private static int offset = 0;
    private static final String table = "public.addresspoints_sam";

    private static Logger logger = LoggerFactory.getLogger(NYSRefreshGeocache.class);

    public void execute(CommandLine opts) throws Exception
    {
        String[] args = opts.getArgs();
        try {
            if (!(args.length == 1)) {
                offset = Integer.parseInt(args[0]);
            }
            logger.info("NYS Refresh Geocache input offset: " + offset);
        }
        catch (Exception e) {
            logger.error(
                    "Check supplied arguments. The only argument should be an int");
            System.exit(-1);
        }
        this.refreshCache(args);
    }

    public void refreshCache(String[] args) {
        NYSRefreshGeocache nysRefreshGeocache = new NYSRefreshGeocache();

        String NYS_BATCH_SQL = "select d.addresslabel, d.citytownname, d.state, d.zipcode, d.latitude, d.longitude, d.pointtype\n" +
                "from public.addresspoints_sam d\n" +
                "order by objectid asc\n" +
                "limit ? OFFSET ?";

        String NYS_COUNT_SQL = "select count(*) from public.addresspoints_sam";

        String GEOCACHE_SELECT =
                "SELECT gc.method \n" +
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
                "where bldgnum = ?  and street = ? and streettype = ? and predir = ? and postdir = ?;";

        HttpClient httpClient = HttpClientBuilder.create().build();
        /*
        Execute SQL
         */
        try {
            //Get total number of addresses that will be used to update our geocache
            Integer total = databaseConfig.jdbcTemplate().queryForObject(NYS_COUNT_SQL, Integer.class);

            //start from 0 and loop until the total number in batches of 2000
            while (total > offset) {
                MapSqlParameterSource params = new MapSqlParameterSource();
                params.addValue("limit", limit);
                params.addValue("offset", offset);
                //Get batch of 2000
                List<NYSGeoAddress> nysGeoAddresses = databaseConfig.namedJdbcTemplate().query(NYS_BATCH_SQL, params,
                        new NysGeoAddressRowMapper());
                logger.info("At offset: " + offset);
                offset = limit + offset;

                //Handle batch
                for (NYSGeoAddress nysGeoAddress: nysGeoAddresses) {
                    //Convert NYSGeoAddressto an Adress Object
                    Address nysAddress = nysGeoAddress.toAddress();

                    //Extract NYSGeoAddress coordinates to form a geocode
                    Geocode nysGeocode = nysGeoAddress.toGeocode();

                    //Convert NYSGeoAddress to a Street address like our cache
                    StreetAddress nysStreetAddress = nysGeoAddress.toStreetAddress();

                    //Run new Address through USPS
                    String httpRequestString = env.getUspsAmsApiUrl()
                            + "validate?detail=true&addr1=%s&addr2=&city=%s&state=%s&zip5=%s&zip4=";
                    httpRequestString = String.format(httpRequestString, nysAddress.getAddr1(),
                            nysAddress.getCity(), nysAddress.getState(), nysAddress.getZip5());
                    httpRequestString = httpRequestString.replaceAll(" ","%20");
                    httpRequestString = StringUtils.deleteWhitespace(httpRequestString);
                    httpRequestString = httpRequestString.replaceAll("`","");

                    try {
                        HttpGet request = new HttpGet(httpRequestString);
                        HttpResponse response = httpClient.execute(request);

                        JSONObject uspsJson = new JSONObject(nysRefreshGeocache.convertStreamToString( response.getEntity().getContent() ) );
                        if (uspsJson.getBoolean("validated") ) {
                            JSONObject uspsAddressJson = uspsJson.getJSONObject("address");
                            nysStreetAddress = StreetAddressParser.parseAddress(new Address(
                                    uspsAddressJson.getString("addr1"), uspsAddressJson.getString("addr2"),
                                    uspsAddressJson.getString("city"), uspsAddressJson.getString("state"),
                                    uspsAddressJson.getString("zip5"), uspsAddressJson.getString("zip4")));
                        }
                    }
                    catch (Exception e) {
                        logger.error("Failed to make Http request because of exception" + e.getMessage());
                        logger.error("Failed address was: " + nysAddress.toString());
                    }

                    MapSqlParameterSource geocacheParams = new MapSqlParameterSource();
                    geocacheParams.addValue("limit", nysStreetAddress.getBldgNum());
                    geocacheParams.addValue("limit", nysStreetAddress.getPreDir());
                    geocacheParams.addValue("limit", nysStreetAddress.getStreetName());
                    geocacheParams.addValue("limit", nysStreetAddress.getPostDir());
                    geocacheParams.addValue("limit", nysStreetAddress.getStreetType());
                    geocacheParams.addValue("limit", nysStreetAddress.getZip5().toString());
                    geocacheParams.addValue("limit", nysStreetAddress.getLocation());

                    //Determine if address exits in our Geocache by getting the method of its results (GoogleDao, YahooDao, etc)
                    String geocachedStreetAddressProvider = databaseConfig.namedJdbcTemplate().queryForObject(GEOCACHE_SELECT, geocacheParams, String.class);

                    //If the geocacheStreetAddressProvider is empty, we don't have the address cached, so insert the address
                    if (StringUtils.isEmpty(geocachedStreetAddressProvider)) {
                        //insert
                        databaseConfig.jdbcTemplate().update(INSERT_GEOCACHE, Integer.valueOf(nysStreetAddress.getBldgNum()),
                                nysStreetAddress.getPreDir(), nysStreetAddress.getStreetName(), nysStreetAddress.getStreetType(),
                                nysStreetAddress.getPostDir(), nysStreetAddress.getLocation(),
                                nysStreetAddress.getState(), nysStreetAddress.getZip5(),
                                "POINT(" + nysGeocode.getLon() + " " + nysGeocode.getLat() + ")",
                                nysGeocode.getMethod(), nysGeocode.getQuality().name(), nysStreetAddress.getZip4());
                    }
                    //If the provider is not Google and NYS Geo has a rooftop coordinate, update the cache
                    else if (!geocachedStreetAddressProvider.equals("GoogleDao") && nysGeoAddress.getPointtype() == 1) {
                        //update
                        databaseConfig.jdbcTemplate().update(UPDATE_GEOCACHE,
                                "POINT(" + nysGeocode.getLon() + " " + nysGeocode.getLat() + ")",
                                nysGeocode.getMethod(), nysGeocode.getQuality().name(), nysStreetAddress.getZip4(),
                                nysStreetAddress.getBldgNum(), nysStreetAddress.getStreetName(),
                                nysStreetAddress.getStreetType(), nysStreetAddress.getPreDir(),
                                nysStreetAddress.getPostDir()
                        );
                    }
                }
            }

            try {
                ((CloseableHttpClient) httpClient).close();
            }
            catch (IOException e) {
                logger.error("Failed to close http connection \n" + e);
            }

        }
        catch (Exception ex) {
            logger.error("Error retrieving addresses from geocache \n" + ex);
        }
    }

    private String convertStreamToString(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
        return sb.toString();
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

    public static void main(String[] args) throws Exception {
        logger.info("running");
        CommandLine cmd = getCommandLine(new Options(), args);
        new NYSRefreshGeocache().execute(cmd);
    }

    public static class NysGeoAddressRowMapper implements RowMapper<NYSGeoAddress> {

        @Override
        public NYSGeoAddress mapRow(ResultSet rs, int rowNum) throws SQLException {
            NYSGeoAddress nysGeoAddress = new NYSGeoAddress();
            nysGeoAddress.setAddresslabel(rs.getString("addresslabel"));
            nysGeoAddress.setCitytownname(rs.getString("citytownname"));
            nysGeoAddress.setState(rs.getString("state"));
            nysGeoAddress.setZipcode(rs.getString("zipcode"));
            nysGeoAddress.setLatitude(rs.getDouble("latitude"));
            nysGeoAddress.setLongitude(rs.getDouble("longitude"));
            nysGeoAddress.setPointtype(rs.getInt("pointtype"));
            return nysGeoAddress;
        }
    }
}
