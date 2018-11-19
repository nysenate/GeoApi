package gov.nysenate.sage.scripts;

import gov.nysenate.sage.config.DatabaseConfig;
import gov.nysenate.sage.config.Environment;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class BatchZipGeocache extends BaseScript {

    @Autowired
    Environment env;

    @Autowired
    DatabaseConfig databaseConfig;

    private static Logger logger = LoggerFactory.getLogger(BatchZipGeocache.class);

    public void execute(CommandLine opts) throws Exception
    {
        String[] args = opts.getArgs();
        this.batchGeocodeZips();
    }

    public void batchGeocodeZips() {
        String GET_ZIP_SQL = "select zcta5ce10 from districts.zip;";

        String BASE_URL = env.getBaseUrl() + "/api/v2/geo/geocode?addr=";
        String GEO_PROVIDER_URL = "&provider=google";
        List<String> zipCodes = null;

        /*
        Execute SQL and get zip codes
         */
        try {
            zipCodes = databaseConfig.jdbcTemplate().query(GET_ZIP_SQL, (rs, rowNum) -> rs.getString("zcta5ce10"));
        }
        catch (Exception ex) {
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
                logger.error("Failed to make Http request because of exception" + e.getMessage());
                System.exit(-1);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        logger.info("running");
        CommandLine cmd = getCommandLine(new Options(), args);
        new BatchZipGeocache().execute(cmd);
    }
}
