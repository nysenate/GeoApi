package gov.nysenate.sage.scripts;

import gov.nysenate.sage.config.Environment;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.net.HttpURLConnection;
import java.net.URL;

import static gov.nysenate.sage.scripts.BaseScript.getCommandLine;
import static org.junit.Assert.*;

@Component
public class ProdReadinessTest {

    @Autowired
    Environment env;

    private static Logger logger = LoggerFactory.getLogger(ProdReadinessTest.class);

    public String json = "[ { \"addr1\" : \"100 Nyroy Dr\", \"addr2\" : \"\", \"city\" : \"Troy\", \"state\" : \"NY\", \"zip5\" : \"12180\", \"zip4\" : \"\" }, "
            + "{ \"addr1\" : \"44 Fairlawn Ave\", \"addr2\" : \"\", \"city\" : \"Albany\", \"state\" : \"NY\", \"zip5\" : \"12203\", \"zip4\" : \"\" }, "
            + "{ \"addr1\" : \"903 London Square Drive\", \"addr2\" : \"\", \"city\" : \"Clifton Park\", \"state\" : \"NY\", \"zip5\" : \"12065\", \"zip4\" : \"\" },"
            + "{ \"addr1\" : \"2825 Kingsland Ave\", \"addr2\" : \"\", \"city\" : \"New York\", \"state\" : \"NY\", \"zip5\" : \"10496\", \"zip4\" : \"\" },"
            + "{ \"addr1\" : \"535 Highland Ave\", \"addr2\" : \"\", \"city\" : \"Rochester\", \"state\" : \"NY\", \"zip5\" : \"14620\", \"zip4\" : \"\" },"
            + "{ \"addr1\" : \"46-08 74th Street\", \"addr2\" : \"\", \"city\" : \"flushing\", \"state\" : \"NY\", \"zip5\" : \"11373\", \"zip4\" : \"\" }"
            + "{ \"addr1\" : \"200 state street\", \"addr2\" : \"\", \"city\" : \"albany\", \"state\" : \"NY\", \"zip5\" : \"12210\", \"zip4\" : \"\" }]";

    public void execute(CommandLine opts) throws Exception
    {
        String[] args = opts.getArgs();
        this.testReadiness(args);
    }

    public void testReadiness(String[] args) throws Exception {
        ProdReadinessTest prodReadinessTest = new ProdReadinessTest();

        String baseUrl = env.getBaseUrl();

        //Test Address Api Functionality
        HttpURLConnection addressValidate = prodReadinessTest.createHttpRequest(
               baseUrl, "/api/v2/address/validate?addr1=44 Fairlawn Avenue&city=Albany&state=NY");
        assertEquals(200, addressValidate.getResponseCode());
        addressValidate.disconnect();

        HttpURLConnection citystateValidate = prodReadinessTest.createHttpRequest(
                baseUrl,
                "/api/v2/address/citystate?zip5=12210");
        assertEquals(200, citystateValidate.getResponseCode());
        citystateValidate.disconnect();

        HttpURLConnection zipcodeValidate = prodReadinessTest.createHttpRequest(
                baseUrl,
                "/api/v2/address/zipcode?addr1=44 Fairlawn Avenue&city=Albany&state=NY");
        assertEquals(200, zipcodeValidate.getResponseCode());
        zipcodeValidate.disconnect();


        //Test Geo Api Functionality
        HttpURLConnection standardGeoValidate = prodReadinessTest.createHttpRequest(
                baseUrl,
                "/api/v2/geo/geocode?addr=200 State St, Albany NY 12210");
        assertEquals(200, standardGeoValidate.getResponseCode());
        standardGeoValidate.disconnect();

        HttpURLConnection splitGeoValidate = prodReadinessTest.createHttpRequest(
                baseUrl,
                "/api/v2/geo/geocode?addr1=200 State St&city=Albany&state=NY&zip5=12210");
        assertEquals(200, splitGeoValidate.getResponseCode());
        splitGeoValidate.disconnect();

        HttpURLConnection providerGeocacheValidate = prodReadinessTest.createHttpRequest(
                baseUrl,
                "/api/v2/geo/geocode?addr1=200 State St&city=Albany&state=NY&zip5=12210&provider=geocache");
        assertEquals(200, providerGeocacheValidate.getResponseCode());
        providerGeocacheValidate.disconnect();

        HttpURLConnection providerGoogleValidate = prodReadinessTest.createHttpRequest(
                baseUrl,
                "/api/v2/geo/geocode?addr1=200 State St&city=Albany&state=NY&zip5=12210&provider=google");
        assertEquals(200, providerGoogleValidate.getResponseCode());
        providerGoogleValidate.disconnect();

        HttpURLConnection providerTigerValidate = prodReadinessTest.createHttpRequest(
                baseUrl,
                "/api/v2/geo/geocode?addr1=200 State St&city=Albany&state=NY&zip5=12210&provider=tiger");
        assertEquals(200, providerTigerValidate.getResponseCode());
        providerTigerValidate.disconnect();

        HttpURLConnection bypassCacheUseFallBackValidate = prodReadinessTest.createHttpRequest(
                baseUrl,
                "/api/v2/geo/geocode?addr1=200 State St&city=Albany&state=NY&zip5=12210&bypassCache=true&useFallBack=true");
        assertEquals(200, bypassCacheUseFallBackValidate.getResponseCode());
        bypassCacheUseFallBackValidate.disconnect();

        HttpURLConnection revGeocodeValidate = prodReadinessTest.createHttpRequest(
                baseUrl,
                "/api/v2/geo/revgeocode?lat=42.652030&lon=-73.757590");
        assertEquals(200, revGeocodeValidate.getResponseCode());
        revGeocodeValidate.disconnect();


        //Test Street Api
        HttpURLConnection streetValidate = prodReadinessTest.createHttpRequest(
                baseUrl,
                "/api/v2/street/lookup?zip5=12210");
        assertEquals(200, streetValidate.getResponseCode());
        streetValidate.disconnect();


        //Test Bluebird District Assignment api
        HttpURLConnection standardBluebirdValidate = prodReadinessTest.createHttpRequest(
                baseUrl,
                "/api/v2/district/bluebird?addr=280 Madison Ave NY");
        assertEquals(200, standardBluebirdValidate.getResponseCode());
        standardBluebirdValidate.disconnect();

        HttpURLConnection splitBluebirdValidate = prodReadinessTest.createHttpRequest(
                baseUrl,
                "/api/v2/district/bluebird?addr1=280 Madison Ave&state=NY");
        assertEquals(200, splitBluebirdValidate.getResponseCode());
        splitBluebirdValidate.disconnect();


        //Test Standard District Assignment
        HttpURLConnection standardDistAssignValidate = prodReadinessTest.createHttpRequest(
                baseUrl,
                "/api/v2/district/assign?addr=280 Madison Ave, New York, NY");
        assertEquals(200, standardDistAssignValidate.getResponseCode());
        standardDistAssignValidate.disconnect();

        HttpURLConnection splitDistAssignValidate = prodReadinessTest.createHttpRequest(
                baseUrl,
                "/api/v2/district/assign?addr1=280 Madison Ave&city=New York&state=NY");
        assertEquals(200, splitDistAssignValidate.getResponseCode());
        splitDistAssignValidate.disconnect();

        HttpURLConnection standardUIDistAssignValidate = prodReadinessTest.createHttpRequest(
                baseUrl,
                "/api/v2/district/assign?addr=200 State Street,Albany,NY,USA&uspsValidate=true&showMaps=true&showMembers=true&showMultiMatch=true");
        assertEquals(200, standardUIDistAssignValidate.getResponseCode());
        standardUIDistAssignValidate.disconnect();

        HttpURLConnection geoProviderGoogleDistAssignValidate = prodReadinessTest.createHttpRequest(
                baseUrl,
                "/api/v2/district/assign?addr1=280 Madison Ave&city=New York&state=NY&geoProvider=google");
        assertEquals(200, geoProviderGoogleDistAssignValidate.getResponseCode());
        geoProviderGoogleDistAssignValidate.disconnect();

        HttpURLConnection geoProviderTigerDistAssignValidate = prodReadinessTest.createHttpRequest(
                baseUrl,
                "/api/v2/district/assign?addr1=280 Madison Ave&city=New York&state=NYgeoProvider=tiger");
        assertEquals(200, geoProviderTigerDistAssignValidate.getResponseCode());
        geoProviderTigerDistAssignValidate.disconnect();

        HttpURLConnection providerStreetfileDistAssignValidate = prodReadinessTest.createHttpRequest(
                baseUrl,
                "/api/v2/district/assign?addr=200%20State%20Street,%20Albany,%20NY,%20USA&provider=streetfile&uspsValidate=true&showMaps=true&showMembers=true&showMultiMatch=true" );
        assertEquals(200, providerStreetfileDistAssignValidate.getResponseCode());
        providerStreetfileDistAssignValidate.disconnect();

        HttpURLConnection providerShapefileDistAssignValidate = prodReadinessTest.createHttpRequest(
                baseUrl,
                "/api/v2/district/assign?addr=200%20State%20Street,%20Albany,%20NY,%20USA&provider=shapefile&uspsValidate=true&showMaps=true&showMembers=true&showMultiMatch=true");
        assertEquals(200, providerShapefileDistAssignValidate.getResponseCode());
        providerShapefileDistAssignValidate.disconnect();

        HttpURLConnection skipGeocodeStreetfileDistAssignValidate = prodReadinessTest.createHttpRequest(
                baseUrl,
                "/api/v2/district/assign?addr=200%20State%20Street,%20Albany,%20NY,%20USA&provider=streetfile&uspsValidate=true&showMaps=true&showMembers=true&showMultiMatch=true&skipGeocode=true");
        assertEquals(200, skipGeocodeStreetfileDistAssignValidate.getResponseCode());
        skipGeocodeStreetfileDistAssignValidate.disconnect();

        HttpURLConnection RevGeocodeDistAssignValidate = prodReadinessTest.createHttpRequest(
                baseUrl,
                "/api/v2/district/assign?lat=40.751352&lon=-73.980335");
        assertEquals(200, RevGeocodeDistAssignValidate.getResponseCode());
        RevGeocodeDistAssignValidate.disconnect();


        //Geocode Batch Validation
        CloseableHttpResponse standardGeocodeBatchValidate = prodReadinessTest.createHttpPostRequest(
                baseUrl,
                "/api/v2/geo/geocode/batch");
        assertEquals(200, standardGeocodeBatchValidate.getStatusLine().getStatusCode());
        standardGeocodeBatchValidate.close();

        CloseableHttpResponse providerGoogleGeocodeBatchValidate = prodReadinessTest.createHttpPostRequest(
                baseUrl,
                "/api/v2/geo/geocode/batch?provider=google");
        assertEquals(200, providerGoogleGeocodeBatchValidate.getStatusLine().getStatusCode());
        providerGoogleGeocodeBatchValidate.close();

        CloseableHttpResponse providerTigerBatchValidate = prodReadinessTest.createHttpPostRequest(
                baseUrl,
                "/api/v2/geo/geocode/batch?provider=Tiger");
        assertEquals(200, providerTigerBatchValidate.getStatusLine().getStatusCode());
        providerTigerBatchValidate.close();

        //Dist Assign Batch Validation
        CloseableHttpResponse standardDistAssignBatchValidate = prodReadinessTest.createHttpPostRequest(
                baseUrl,
                "/api/v2/district/assign/batch");
        assertEquals(200, standardDistAssignBatchValidate.getStatusLine().getStatusCode());
        standardDistAssignBatchValidate.close();

        //Bluebird Batch Validation
        CloseableHttpResponse standardBluebirdBatchValidate = prodReadinessTest.createHttpPostRequest(
                baseUrl,
                "/api/v2/district/bluebird/batch");
        assertEquals(200, standardBluebirdBatchValidate.getStatusLine().getStatusCode());
        standardBluebirdBatchValidate.close();
    }


    public static void main(String[] args) throws Exception {
        logger.info("running");
        CommandLine cmd = getCommandLine(new Options(), args);
        new ProdReadinessTest().execute(cmd);
    }

    private HttpURLConnection createHttpRequest(String ctxPath, String apiPath) throws Exception {
        apiPath = apiPath.replaceAll(" ","%20");
        logger.info(ctxPath + apiPath + "\n");
        URL url = new URL(ctxPath + apiPath);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setUseCaches(false);
        con.setDoOutput(true);
        con.setInstanceFollowRedirects(true);
        if (con.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP
                || con.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM) {
            String location = con.getHeaderField("Location");
            URL newUrl = new URL(location);
            con = (HttpURLConnection) newUrl.openConnection();
        }
        return con;
    }


    private CloseableHttpResponse createHttpPostRequest(String ctxPath, String apiPath) throws Exception {
        apiPath = apiPath.replaceAll(" ","%20");
        logger.info(ctxPath + apiPath + "\n");
        CloseableHttpClient client = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();
        HttpPost httpPost = new HttpPost(ctxPath + apiPath);
        StringEntity entity = new StringEntity(json);
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        return client.execute(httpPost);
    }
}
