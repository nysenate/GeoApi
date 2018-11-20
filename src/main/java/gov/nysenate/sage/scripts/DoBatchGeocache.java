package gov.nysenate.sage.scripts;

import gov.nysenate.sage.config.DatabaseConfig;
import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.StreetAddress;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static gov.nysenate.sage.scripts.BaseScript.getCommandLine;

@Component
public class DoBatchGeocache {


    @Autowired
    Environment env;
    @Autowired
    DatabaseConfig databaseConfig;

    private static Logger logger = LoggerFactory.getLogger(DoBatchGeocache.class);


    public void execute(CommandLine opts) throws Exception
    {
        String[] args = opts.getArgs();

        if (args.length < 2) {
            logger.error("Usage: DoBatchGeocache offset_number amount_to_geocode_number geocode_yahoo_only \n" +
                    "EX. DoBatchGeocache 0 100 or DoBatchGeocache 0 100 true");
            System.exit(1);
        }

        this.doBatchGeocache(args);
    }

    public String createJsonForAddress(String addr1, String city, String state, String zip5) {
        return "{\"addr1\": " + "\"" + addr1 + "\"" + "," + "\"city\": " + "\"" + city + "\"" + "," +
                "\"state\": " + "\"" + state + "\"" + "," + "\"zip5\": " + "\"" + zip5 + "\"" + "},";
    }

    public void doBatchGeocache(String[] args) {
        /*
        Create necessary variables to execute sql
         */
        long offset = 0;
        long limit = 10;
        boolean yahoo = true;

        String BATCH_SQL =
                "select g.bldgnum, g.predir, g.street, g.streettype, g.postdir, g.location, g.state, g.zip5, g.zip4\n" +
                "from cache.geocache g\n";

        String OPTIONAL_WHERE_YAHOO =
                "where method = 'YahooDao' and state = 'NY' and street != '' and streettype != '' and quality = 'HOUSE'\n";

        String ORDER_SQL = "order by id asc\n" +
                "limit ? OFFSET ?";

        /*
        Create necessary variables to execute http request
         */
        String httpRequestString = env.getBaseUrl() +
                "/api/v2/geo/geocode/batch?provider=google&bypassCache=true&doNotCache=false&useFallback=false";
        String httpRequestBody = "[";

        /*
        Ensure that the arugments passed in are actually numbers / determine if we geocode yahoo only
         */
        try {
            offset = Long.parseLong(args[0]);
            limit = Long.parseLong(args[1]);
            if (args.length == 3) {
                yahoo = Boolean.getBoolean(args[2]);
            }
            logger.info("DoBatchGeocache input offset: " + offset + "\n" +
                    "DoBatchGeocache input limit: " + limit);
        }
        catch (Exception e) {
            logger.error(
                    "Check supplied arguments. The first 2 arguments should be intergers, the 3rd should be a boolean");
            System.exit(-1);
        }

        //Build SQL
        if (yahoo) {
            BATCH_SQL = BATCH_SQL + OPTIONAL_WHERE_YAHOO + ORDER_SQL;
        }
        else {
            BATCH_SQL = BATCH_SQL + ORDER_SQL;
        }

        /*
        Execute SQL and form http request body
         */
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("limit", limit);
            params.addValue("offset", offset);

            List<StreetAddress> streetAddressesToGeocache = databaseConfig.tigerNamedJdbcTemplate()
                    .query(BATCH_SQL, params, new StreetAddressRowMapper() );
             for (StreetAddress streetAddress: streetAddressesToGeocache) {
                 Address address = streetAddress.toAddress();
                 logger.debug(address.toString());
                 httpRequestBody = httpRequestBody + createJsonForAddress(
                         address.getAddr1(),
                         address.getCity(),
                         address.getState(),
                         address.getZip5());
             }

             httpRequestBody = httpRequestBody.substring(0, httpRequestBody.length()-1) + "]";
             logger.debug(httpRequestBody);
        }
        catch (Exception ex) {
            logger.error("Error retrieving addresses from geocache", ex);
        }

        /*
        Execute http request
         */
        HttpClient httpClient = HttpClientBuilder.create().build();
        try {
            HttpPost request = new HttpPost(httpRequestString);
            StringEntity params =new StringEntity(httpRequestBody);
            request.addHeader("content-type", "application/json");
            request.setEntity(params);
            HttpResponse response = httpClient.execute(request);
            logger.info("Batch Request with offset " + offset + " and limit " + limit +
                    " was executed with an http status of: " + response.getStatusLine() );
            ((CloseableHttpClient) httpClient).close();
        }
        catch (Exception e) {
            logger.error("Failed to make Http request because of exception" + e.getMessage());
            System.exit(-1);
        }
    }

    public static void main(String[] args) throws Exception {
        logger.info("running");
        CommandLine cmd = getCommandLine(new Options(), args);
        new DoBatchGeocache().execute(cmd);
    }


    public static class StreetAddressRowMapper implements RowMapper<StreetAddress> {

        @Override
        public StreetAddress mapRow(ResultSet rs, int rowNum) throws SQLException {
            StreetAddress sa =  new StreetAddress();
            sa.setBldgNum(rs.getInt("bldgnum"));
            sa.setPreDir(rs.getString("predir"));
            sa.setStreet(rs.getString("street"));
            sa.setStreetType(rs.getString("streettype"));
            sa.setPostDir(rs.getString("postdir"));
            sa.setLocation(rs.getString("location"));
            sa.setState("state");
            sa.setZip4(rs.getString("zip5"));
            sa.setZip5(rs.getString("zip4"));
            return sa;
        }
    }
}
