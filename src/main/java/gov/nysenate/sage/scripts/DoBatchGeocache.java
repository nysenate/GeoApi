package gov.nysenate.sage.scripts;

import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.StreetAddress;
import gov.nysenate.sage.util.Config;
import gov.nysenate.sage.util.FormatUtil;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.List;

public class DoBatchGeocache {
    private Config config;
    QueryRunner tigerRun;
    private static Logger logger = Logger.getLogger(DoBatchGeocache.class);

    public DoBatchGeocache() {
        config = ApplicationFactory.getConfig();
        tigerRun = new QueryRunner(ApplicationFactory.getTigerDataSource());
    }

    public String createJsonForAddress(String addr1, String city, String state, String zip5) {
        return "{\"addr1\": " + "\"" + addr1 + "\"" + "," + "\"city\": " + "\"" + city + "\"" + "," + "\"state\": " + "\"" + state + "\"" + "," + "\"zip5\": " + "\"" + zip5 + "\"" + "},";
    }

    public static void main(String[] args) {
        /*
        Make sure that there were 2 arguments supplied to the script
         */
        if (args.length != 2) {
            System.err.println("Usage: DoBatchGeocache offset_number amount_to_geocode_number EX. DoBatchGeocache 0 100");
            System.exit(1);
        }

        /* Load up the configuration settings */
        if (!ApplicationFactory.bootstrap()) {
            System.err.println("Failed to configure application");
            System.exit(-1);
        }

        /*
        Create necessary variables to execute sql
         */
        DoBatchGeocache doBatchGeocache = new DoBatchGeocache();
        long offset = 0;
        long limit = 10;
        String BATCH_SQL = "select g.bldgnum, g.predir, g.street, g.streettype, g.postdir, g.location, g.state, g.zip5, g.zip4\n" +
                "from cache.geocache g\n" +
                "order by id asc\n" +
                "limit ? OFFSET ?";
        BeanListHandler<StreetAddress> beanListHandler
                = new BeanListHandler<>(StreetAddress.class);

        /*
        Create necessary variables to execute http request
         */
        String httpRequestString = doBatchGeocache.config.getValue("base.url") + "/api/v2/geo/geocode/batch?provider=google&bypassCache=true&doNotCache=false&useFallback=false";
        String httpRequestBody = "[";

        /*
        Ensure that the arugments passed in are actually numbers
         */
        try {
            offset = Long.parseLong(args[0]);
            limit = Long.parseLong(args[1]);
            logger.info("DoBatchGeocache input offset: " + offset + "\n" +
                    "DoBatchGeocache input limit: " + limit);
        }
        catch (Exception e) {
            System.err.println("Supplied arguments were not integers");
            System.exit(-1);
        }

        /*
        Execute SQL and form http request body
         */
        try {
             List<StreetAddress> streetAddressesToGeocache = doBatchGeocache.tigerRun.query(BATCH_SQL, beanListHandler, limit, offset);
             for (StreetAddress streetAddress: streetAddressesToGeocache) {
                 Address address = streetAddress.toAddress();
                 logger.debug(address.toString());
                 httpRequestBody = httpRequestBody + doBatchGeocache.createJsonForAddress(
                         address.getAddr1(),
                         address.getCity(),
                         address.getState(),
                         address.getZip5());
             }

             httpRequestBody = httpRequestBody.substring(0, httpRequestBody.length()-1) + "]";
             logger.debug(httpRequestBody);
        }
        catch (SQLException ex) {
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
            System.err.println("Failed to make Http request because of exception" + e.getMessage());
            System.exit(-1);
        }
    }
}
