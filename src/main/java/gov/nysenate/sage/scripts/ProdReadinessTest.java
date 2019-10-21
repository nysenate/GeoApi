package gov.nysenate.sage.scripts;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gov.nysenate.sage.model.address.Address;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.function.Consumer;

public class ProdReadinessTest {

    private static Logger logger = LoggerFactory.getLogger(ProdReadinessTest.class);
    private String baseUrl = "http://localhost:8082"; //TODO change to 8080

    private ArrayList<Address> testAddresses = new ArrayList<>();

    public String getBaseUrl() {
        return this.baseUrl;
    }

    private void initializeTestAddresses() {
        testAddresses.add(new Address("100 Nyroy Dr", "Troy", "NY", "12180"));
        testAddresses.add(new Address("44 Fairlawn Ave","Albany","NY","12203"));
        testAddresses.add(new Address("903 London Square Drive","Clifton Park","NY","12065"));
        testAddresses.add(new Address("2825 Kingsland Ave","New York","NY","10496"));
        testAddresses.add(new Address("535 Highland Ave","Rochester","NY","14620"));
        testAddresses.add(new Address("46-08 74th Street","Flushing","NY","11373"));
        testAddresses.add(new Address("200 State Street","Albany","NY","12210"));
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


    private CloseableHttpResponse createHttpPostRequest(String ctxPath, String apiPath, String json) throws Exception {
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

    private JsonNode getResponseAndCloseStream(HttpURLConnection conn) throws IOException {
        JsonNode jsonResponse = getResponseFromInputStream(conn.getInputStream());
        conn.disconnect();
        return jsonResponse;
    }

    private JsonNode getResponseFromInputStream(InputStream is) throws IOException {
        String sageReponse = IOUtils.toString(is, "UTF-8");
        JsonNode jsonResonse = new ObjectMapper().readTree(sageReponse);
        is.close();
        return jsonResonse;
    }

    private String turnAddressesIntoJson() {
        Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJson = prettyGson.toJson(this.testAddresses);
        return prettyJson;
    }

    private int standardSuccessResponseCheck(JsonNode jsonResponse) {
        return jsonResponse.get("statusCode").asInt();
    }

    private void batchSuccessResponseCheck(JsonNode jsonResponse) {
        JsonNode results = jsonResponse.get("results");
        results.forEach( (JsonNode addrResponse) ->
                assertEquals(0,standardSuccessResponseCheck(addrResponse) ));
    }

    public static void main(String[] args) throws Exception {
        ProdReadinessTest prodReadinessTest = new ProdReadinessTest();
        prodReadinessTest.initializeTestAddresses();
        String baseUrl = prodReadinessTest.getBaseUrl();
        JsonNode jsonResponse;

        String json = prodReadinessTest.turnAddressesIntoJson();

        /**
         *Test Address Api Functionality
         */
        HttpURLConnection addressValidate = prodReadinessTest.createHttpRequest(
               baseUrl, "/api/v2/address/validate?addr1=44 Fairlawn Avenue&city=Albany&state=NY");
        jsonResponse = prodReadinessTest.getResponseAndCloseStream(addressValidate);
        assertEquals(0, prodReadinessTest.standardSuccessResponseCheck(jsonResponse)); //Expected - Actual

        HttpURLConnection citystateValidate = prodReadinessTest.createHttpRequest(
                baseUrl,
                "/api/v2/address/citystate?zip5=12210");
        jsonResponse = prodReadinessTest.getResponseAndCloseStream(citystateValidate);
        assertEquals(0, prodReadinessTest.standardSuccessResponseCheck(jsonResponse));


        HttpURLConnection zipcodeValidate = prodReadinessTest.createHttpRequest(
                baseUrl,
                "/api/v2/address/zipcode?addr1=44 Fairlawn Avenue&city=Albany&state=NY");
        jsonResponse = prodReadinessTest.getResponseAndCloseStream(zipcodeValidate);
        assertEquals(0, prodReadinessTest.standardSuccessResponseCheck(jsonResponse));


        /**
         * Test Geo Api Functionality
         */
        HttpURLConnection standardGeoValidate = prodReadinessTest.createHttpRequest(
                baseUrl,
                "/api/v2/geo/geocode?addr=200 State St, Albany NY 12210");
        jsonResponse = prodReadinessTest.getResponseAndCloseStream(standardGeoValidate);
        assertEquals(0, prodReadinessTest.standardSuccessResponseCheck(jsonResponse));


        HttpURLConnection splitGeoValidate = prodReadinessTest.createHttpRequest(
                baseUrl,
                "/api/v2/geo/geocode?addr1=200 State St&city=Albany&state=NY&zip5=12210");
        jsonResponse = prodReadinessTest.getResponseAndCloseStream(splitGeoValidate);
        assertEquals(0, prodReadinessTest.standardSuccessResponseCheck(jsonResponse));


        HttpURLConnection providerGeocacheValidate = prodReadinessTest.createHttpRequest(
                baseUrl,
                "/api/v2/geo/geocode?addr1=200 State St&city=Albany&state=NY&zip5=12210&provider=geocache");
        jsonResponse = prodReadinessTest.getResponseAndCloseStream(providerGeocacheValidate);
        assertEquals(0, prodReadinessTest.standardSuccessResponseCheck(jsonResponse));


        HttpURLConnection providerGoogleValidate = prodReadinessTest.createHttpRequest(
                baseUrl,
                "/api/v2/geo/geocode?addr1=200 State St&city=Albany&state=NY&zip5=12210&provider=google");
        jsonResponse = prodReadinessTest.getResponseAndCloseStream(providerGoogleValidate);
        assertEquals(0, prodReadinessTest.standardSuccessResponseCheck(jsonResponse));


        HttpURLConnection providerTigerValidate = prodReadinessTest.createHttpRequest(
                baseUrl,
                "/api/v2/geo/geocode?addr1=200 State St&city=Albany&state=NY&zip5=12210&provider=tiger");
        jsonResponse = prodReadinessTest.getResponseAndCloseStream(providerTigerValidate);
        assertEquals(0, prodReadinessTest.standardSuccessResponseCheck(jsonResponse));


        HttpURLConnection bypassCacheUseFallBackValidate = prodReadinessTest.createHttpRequest(
                baseUrl,
                "/api/v2/geo/geocode?addr1=200 State St&city=Albany&state=NY&zip5=12210&bypassCache=true&useFallBack=true");
        jsonResponse = prodReadinessTest.getResponseAndCloseStream(bypassCacheUseFallBackValidate);
        assertEquals(0, prodReadinessTest.standardSuccessResponseCheck(jsonResponse));


        HttpURLConnection revGeocodeValidate = prodReadinessTest.createHttpRequest(
                baseUrl,
                "/api/v2/geo/revgeocode?lat=42.6533668&lon=-73.7621715");
        jsonResponse = prodReadinessTest.getResponseAndCloseStream(revGeocodeValidate);
        assertEquals(0, prodReadinessTest.standardSuccessResponseCheck(jsonResponse));


        /**
         * Test Street Api
         */
        HttpURLConnection streetValidate = prodReadinessTest.createHttpRequest(
                baseUrl,
                "/api/v2/street/lookup?zip5=12210");
        jsonResponse = prodReadinessTest.getResponseAndCloseStream(streetValidate);
        assertEquals(0, prodReadinessTest.standardSuccessResponseCheck(jsonResponse));


        /**
         * Test Bluebird District Assignment api
         */
        HttpURLConnection standardBluebirdValidate = prodReadinessTest.createHttpRequest(
                baseUrl,
                "/api/v2/district/bluebird?addr=280 Madison Ave NY");
        jsonResponse = prodReadinessTest.getResponseAndCloseStream(standardBluebirdValidate);
        assertEquals(0, prodReadinessTest.standardSuccessResponseCheck(jsonResponse));


        HttpURLConnection splitBluebirdValidate = prodReadinessTest.createHttpRequest(
                baseUrl,
                "/api/v2/district/bluebird?addr1=280 Madison Ave&state=NY");
        jsonResponse = prodReadinessTest.getResponseAndCloseStream(splitBluebirdValidate);
        assertEquals(0, prodReadinessTest.standardSuccessResponseCheck(jsonResponse));



        /**
         * Test Standard District Assignment
         */
        HttpURLConnection standardDistAssignValidate = prodReadinessTest.createHttpRequest(
                baseUrl,
                "/api/v2/district/assign?addr=280 Madison Ave, New York, NY");
        jsonResponse = prodReadinessTest.getResponseAndCloseStream(standardDistAssignValidate);
        assertEquals(0, prodReadinessTest.standardSuccessResponseCheck(jsonResponse));


        HttpURLConnection splitDistAssignValidate = prodReadinessTest.createHttpRequest(
                baseUrl,
                "/api/v2/district/assign?addr1=280 Madison Ave&city=New York&state=NY");
        jsonResponse = prodReadinessTest.getResponseAndCloseStream(splitDistAssignValidate);
        assertEquals(0, prodReadinessTest.standardSuccessResponseCheck(jsonResponse));


        HttpURLConnection standardUIDistAssignValidate = prodReadinessTest.createHttpRequest(
                baseUrl,
                "/api/v2/district/assign?addr=200 State Street,Albany,NY,USA&uspsValidate=true&showMaps=true&showMembers=true&showMultiMatch=true");
        jsonResponse = prodReadinessTest.getResponseAndCloseStream(standardUIDistAssignValidate);
        assertEquals(0, prodReadinessTest.standardSuccessResponseCheck(jsonResponse));


        HttpURLConnection geoProviderGoogleDistAssignValidate = prodReadinessTest.createHttpRequest(
                baseUrl,
                "/api/v2/district/assign?addr1=280 Madison Ave&city=New York&state=NY&geoProvider=google");
        jsonResponse = prodReadinessTest.getResponseAndCloseStream(geoProviderGoogleDistAssignValidate);
        assertEquals(0, prodReadinessTest.standardSuccessResponseCheck(jsonResponse));


        HttpURLConnection geoProviderTigerDistAssignValidate = prodReadinessTest.createHttpRequest(
                baseUrl,
                "/api/v2/district/assign?addr1=280 Madison Ave&city=New York&state=NY&geoProvider=tiger");
        jsonResponse = prodReadinessTest.getResponseAndCloseStream(geoProviderTigerDistAssignValidate);
        assertEquals(0, prodReadinessTest.standardSuccessResponseCheck(jsonResponse));


        HttpURLConnection providerStreetfileDistAssignValidate = prodReadinessTest.createHttpRequest(
                baseUrl,
                "/api/v2/district/assign?addr=200%20State%20Street,%20Albany,%20NY,%20USA&provider=streetfile&uspsValidate=true&showMaps=true&showMembers=true&showMultiMatch=true" );
        jsonResponse = prodReadinessTest.getResponseAndCloseStream(providerStreetfileDistAssignValidate);
        assertEquals(0, prodReadinessTest.standardSuccessResponseCheck(jsonResponse));


        HttpURLConnection providerShapefileDistAssignValidate = prodReadinessTest.createHttpRequest(
                baseUrl,
                "/api/v2/district/assign?addr=200%20State%20Street,%20Albany,%20NY,%20USA&provider=shapefile&uspsValidate=true&showMaps=true&showMembers=true&showMultiMatch=true");
        jsonResponse = prodReadinessTest.getResponseAndCloseStream(providerShapefileDistAssignValidate);
        assertEquals(0, prodReadinessTest.standardSuccessResponseCheck(jsonResponse));


        HttpURLConnection revGeocodeDistAssignValidate = prodReadinessTest.createHttpRequest(
                baseUrl,
                "/api/v2/district/assign?lat=40.751352&lon=-73.980335");
        jsonResponse = prodReadinessTest.getResponseAndCloseStream(revGeocodeDistAssignValidate);
        assertEquals(0, prodReadinessTest.standardSuccessResponseCheck(jsonResponse));


        /**
         * Geocode Batch Validation
         */
        CloseableHttpResponse standardGeocodeBatchValidate = prodReadinessTest.createHttpPostRequest(
                baseUrl,
                "/api/v2/geo/geocode/batch", json);
        jsonResponse = prodReadinessTest.getResponseFromInputStream(standardGeocodeBatchValidate.getEntity().getContent());
        standardGeocodeBatchValidate.close();
        prodReadinessTest.batchSuccessResponseCheck(jsonResponse);

        CloseableHttpResponse providerGoogleGeocodeBatchValidate = prodReadinessTest.createHttpPostRequest(
                baseUrl,
                "/api/v2/geo/geocode/batch?provider=google", json);
        jsonResponse = prodReadinessTest.getResponseFromInputStream(providerGoogleGeocodeBatchValidate.getEntity().getContent());
        providerGoogleGeocodeBatchValidate.close();
        prodReadinessTest.batchSuccessResponseCheck(jsonResponse);

        CloseableHttpResponse providerTigerBatchValidate = prodReadinessTest.createHttpPostRequest(
                baseUrl,
                "/api/v2/geo/geocode/batch?provider=tiger", json);
        jsonResponse = prodReadinessTest.getResponseFromInputStream(providerTigerBatchValidate.getEntity().getContent());
        providerTigerBatchValidate.close();
        prodReadinessTest.batchSuccessResponseCheck(jsonResponse);

        /**
         * Dist Assign Batch Validation
         */
        CloseableHttpResponse standardDistAssignBatchValidate = prodReadinessTest.createHttpPostRequest(
                baseUrl,
                "/api/v2/district/assign/batch", json);
        jsonResponse = prodReadinessTest.getResponseFromInputStream(standardDistAssignBatchValidate.getEntity().getContent());
        standardDistAssignBatchValidate.close();
        prodReadinessTest.batchSuccessResponseCheck(jsonResponse);

        /**
         * Bluebird Batch Validation
         */
        CloseableHttpResponse standardBluebirdBatchValidate = prodReadinessTest.createHttpPostRequest(
                baseUrl,
                "/api/v2/district/bluebird/batch", json);
        jsonResponse = prodReadinessTest.getResponseFromInputStream(standardBluebirdBatchValidate.getEntity().getContent());
        standardBluebirdBatchValidate.close();
        prodReadinessTest.batchSuccessResponseCheck(jsonResponse);
    }
}
