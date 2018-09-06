package gov.nysenate.sage.scripts;

import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.util.Config;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.List;

public class BatchZipGeocache {

    static Config config;
    QueryRunner geoApiRun;
    private static Logger logger = Logger.getLogger(BatchZipGeocache.class);

    public BatchZipGeocache() {
        config = ApplicationFactory.getConfig();
        geoApiRun = new QueryRunner(ApplicationFactory.getDataSource());
    }

    public static void main(String[] args) {


        /* Load up the configuration settings */
        if (!ApplicationFactory.bootstrap()) {
            System.err.println("Failed to configure application");
            System.exit(-1);
        }

        BatchZipGeocache batchZipGeocache = new BatchZipGeocache();


        String GET_ZIP_SQL = "select zcta5ce10 from districts.zip;";

        String BASE_URL = BatchZipGeocache.config.getValue("base.url") + "/api/v2/district/assign?addr=";

        String GEO_PROVIDER_URL = "&geoProvider=google";

        List<String> zipCodes = null;

        /*
        Execute SQL and get zip codes
         */
        try {
            zipCodes = batchZipGeocache.geoApiRun.query(GET_ZIP_SQL, new ColumnListHandler<String>("zcta5ce10"));
        }
        catch (SQLException ex) {
            logger.error("Error retrieving zip codes from geoapi db", ex);
        }

        //Fail if we couldnt get zip codes properly
        if (zipCodes == null) {
            System.exit(1);
        }


        //Cycle through zip codes
        for (String zip: zipCodes) {
            logger.info("Geocoding zip: " +zip);
            /*
        Execute http request
         */
            HttpClient httpClient = HttpClientBuilder.create().build();
            try {
                HttpPost request = new HttpPost(BASE_URL + zip + GEO_PROVIDER_URL);
                httpClient.execute(request);
                ((CloseableHttpClient) httpClient).close();
            }
            catch (Exception e) {
                System.err.println("Failed to make Http request because of exception" + e.getMessage());
                System.exit(-1);
            }
        }


        ApplicationFactory.close();
        System.exit(0);

    }
}
