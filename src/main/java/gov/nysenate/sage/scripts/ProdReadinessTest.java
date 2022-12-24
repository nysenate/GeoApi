package gov.nysenate.sage.scripts;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.geo.Point;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;

public class ProdReadinessTest {

    private static Logger logger = LoggerFactory.getLogger(ProdReadinessTest.class);
    private String baseUrl = "http://localhost:8080";

    Properties prop =  new Properties();
    private ArrayList<Address> testAddresses = new ArrayList<>();

    private ArrayList<Address> badTestAddresses = new ArrayList<>();
    private ArrayList<Point> testPoints = new ArrayList<>();
    private ArrayList<Integer> testZips = new ArrayList<>();

    public String getBaseUrl() {
        return this.baseUrl;
    }

    private void initializeProperties() throws IOException, NullPointerException {
        InputStream appPropsStream = getClass().getResourceAsStream("/app.properties");
        this.prop.load(appPropsStream);
        this.baseUrl = this.prop.getProperty("base.url");
    }

    private void initializeTestAddresses() {

        badTestAddresses.add(new Address("","Albany","NY","12205",6));
        badTestAddresses.add(new Address("25 Smithtown Circle","Smithtown", "NY","11787",13));
        badTestAddresses.add(new Address("25 Smithtown","Smithtown", "NY","11787",14));

        testAddresses.add(new Address("100 Nyroy Dr", "Troy", "NY", "12180", 1));
        testAddresses.add(new Address("44 Fairlawn Ave","Albany","NY","12203",2));
        testAddresses.add(new Address("903 London Square Drive","Clifton Park","NY","12065",3));
        testAddresses.add(new Address("535 Highland Ave","Rochester","NY","14620", 4));
        testAddresses.add(new Address("46-08 74th Street","Flushing","NY","11373", 5));
        testAddresses.add(new Address("200 State Street","Albany","NY","12210", 6));
    }



    private void initializeTestPoints() { //These correspond directly to the addresses in the testAddresses array
        testPoints.add(new Point(42.7410467,-73.6691371));
        testPoints.add(new Point(42.6711474,-73.79940049999999));
        testPoints.add(new Point(42.8666825,-73.8010151));
        testPoints.add(new Point(40.8677979,-73.83986170000001));
        testPoints.add(new Point(43.13010999999999,-77.5993111));
        testPoints.add(new Point(40.7397789,-73.88993359999999));
        testPoints.add(new Point(42.6533668, -73.7599828));
    }

    private void initializeTestZips() {
        testZips.add(12180);
        testZips.add(12203);
        testZips.add(12065);
        testZips.add(14620);
        testZips.add(11373);
        testZips.add(12210);
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
        return convertObjToJson(this.testAddresses);
    }

    private String turnBadAddressesIntoJson() {
        return convertObjToJson(this.badTestAddresses);
    }

    private String turnPointsIntoJson() {
        return convertObjToJson(this.testPoints);
    }

    private String convertObjToJson(ArrayList arrayList) {
        Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
        return prettyGson.toJson(arrayList);
    }

    private int standardSuccessResponseCheck(JsonNode jsonResponse) {
        return jsonResponse.get("statusCode").asInt();
    }

    private void batchSuccessResponseCheck(JsonNode jsonResponse) {
        JsonNode results = jsonResponse.get("results");
        results.forEach( (JsonNode addrResponse) ->
                assertEquals(0,standardSuccessResponseCheck(addrResponse) ));
    }

    private void cityStateBatchResponseCheck(JsonNode jsonResponse) {
        JsonNode results = jsonResponse.get("results");
        results.forEach( (JsonNode result) ->
                assertEquals(0, result.get("statusCode").asInt() ));
    }

    private void addressBatchValidateResponseCheck(JsonNode jsonResponse) {
        JsonNode results = jsonResponse.get("results");
        results.forEach( (JsonNode result) ->
                assertEquals("\"SUCCESS\"", result.get("status").toString() ));
    }

    private void badAddressBatchValidateResponseCheck(JsonNode jsonResponse) {
        JsonNode results = jsonResponse.get("results");
        results.forEach( (JsonNode result) ->
                assertEquals("\"NO_ADDRESS_VALIDATE_RESULT\"", result.get("status").toString() ));
    }

    public static void main(String[] args) throws Exception {
        ProdReadinessTest prodReadinessTest = new ProdReadinessTest();

        /**
         * Gets the base url from app.properties and ensures the content for testing is ready
         */
        try {
            prodReadinessTest.initializeProperties();
        }
        catch (IOException e) {
            logger.warn("Unable to initialize properties" + e);
        }
        catch (NullPointerException e) {
            logger.warn("Unable to initialize properties" + e);
        }
        prodReadinessTest.initializeTestAddresses();
        prodReadinessTest.initializeTestPoints();
        prodReadinessTest.initializeTestZips();


        /**
         * Common variables used by the test api calls
         */
        String baseUrl = prodReadinessTest.getBaseUrl();
        JsonNode jsonResponse;
        String addressJson = prodReadinessTest.turnAddressesIntoJson();
        String badAddressJson = prodReadinessTest.turnBadAddressesIntoJson();
        String pointJson = prodReadinessTest.turnPointsIntoJson();

        /**
         *Test Address Api Functionality
         */
        HttpURLConnection addressValidate = prodReadinessTest.createHttpRequest(
               baseUrl, "/api/v2/address/validate?addr1=44 Fairlawn Avenue&city=Albany&state=NY");
        jsonResponse = prodReadinessTest.getResponseAndCloseStream(addressValidate);
        assertEquals(0, prodReadinessTest.standardSuccessResponseCheck(jsonResponse)); //Expected - Actual

        HttpURLConnection providerAisAddressValidate = prodReadinessTest.createHttpRequest(
                baseUrl, "/api/v2/address/validate?addr1=44 Fairlawn Avenue&city=Albany&state=NY&provider=uspsais");
        jsonResponse = prodReadinessTest.getResponseAndCloseStream(providerAisAddressValidate);
        assertEquals(0, prodReadinessTest.standardSuccessResponseCheck(jsonResponse));

        HttpURLConnection cityStateValidate = prodReadinessTest.createHttpRequest(
                baseUrl,
                "/api/v2/address/citystate?zip5=12210");
        jsonResponse = prodReadinessTest.getResponseAndCloseStream(cityStateValidate);
        assertEquals(0, prodReadinessTest.standardSuccessResponseCheck(jsonResponse));

        HttpURLConnection providerAisCityStateValidate = prodReadinessTest.createHttpRequest(
                baseUrl,
                "/api/v2/address/citystate?zip5=12210&provider=uspsais");
        jsonResponse = prodReadinessTest.getResponseAndCloseStream(providerAisCityStateValidate);
        assertEquals(0, prodReadinessTest.standardSuccessResponseCheck(jsonResponse));


        HttpURLConnection zipcodeValidate = prodReadinessTest.createHttpRequest(
                baseUrl,
                "/api/v2/address/zipcode?addr1=44 Fairlawn Avenue&city=Albany&state=NY");
        jsonResponse = prodReadinessTest.getResponseAndCloseStream(zipcodeValidate);
        assertEquals(0, prodReadinessTest.standardSuccessResponseCheck(jsonResponse));

        HttpURLConnection providerAisZipcodeValidate = prodReadinessTest.createHttpRequest(
                baseUrl,
                "/api/v2/address/zipcode?addr1=44 Fairlawn Avenue&city=Albany&state=NY&provider=uspsais");
        jsonResponse = prodReadinessTest.getResponseAndCloseStream(providerAisZipcodeValidate);
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


        HttpURLConnection providerNYSGeoValidate = prodReadinessTest.createHttpRequest(
                baseUrl,
                "/api/v2/geo/geocode?addr1=200 State St&city=Albany&state=NY&zip5=12210&provider=nysgeo");
        jsonResponse = prodReadinessTest.getResponseAndCloseStream(providerNYSGeoValidate);
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
                "/api/v2/district/assign?addr1=280 Madison Ave&city=New York&state=NY&geoProvider=nysgeo");
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
         * Address Batch Validation
         */
        CloseableHttpResponse addressBatchValidate = prodReadinessTest.createHttpPostRequest(
                baseUrl,
                "/api/v2/address/validate/batch", addressJson);
        jsonResponse = prodReadinessTest.getResponseFromInputStream(addressBatchValidate.getEntity().getContent());
        logger.info("ADDRESS VALIDATION BATCH: " + jsonResponse);
        addressBatchValidate.close();
        prodReadinessTest.addressBatchValidateResponseCheck(jsonResponse);

        CloseableHttpResponse badAddressBatchValidate = prodReadinessTest.createHttpPostRequest(
                baseUrl,
                "/api/v2/address/validate/batch", badAddressJson);
        jsonResponse = prodReadinessTest.getResponseFromInputStream(badAddressBatchValidate.getEntity().getContent());
        logger.info("BAD ADDRESS VALIDATION BATCH: " + jsonResponse);
        badAddressBatchValidate.close();
        prodReadinessTest.badAddressBatchValidateResponseCheck(jsonResponse);

        CloseableHttpResponse providerAisAddressBatchValidate = prodReadinessTest.createHttpPostRequest(
                baseUrl,
                "/api/v2/address/validate/batch?provider=uspsais", addressJson);
        jsonResponse = prodReadinessTest.getResponseFromInputStream(providerAisAddressBatchValidate.getEntity().getContent());
        providerAisAddressBatchValidate.close();
        prodReadinessTest.addressBatchValidateResponseCheck(jsonResponse);


        CloseableHttpResponse cityStateBatchValidate = prodReadinessTest.createHttpPostRequest(
                baseUrl,
                "/api/v2/address/citystate/batch", addressJson);
        jsonResponse = prodReadinessTest.getResponseFromInputStream(cityStateBatchValidate.getEntity().getContent());
        cityStateBatchValidate.close();
        prodReadinessTest.cityStateBatchResponseCheck(jsonResponse);

        CloseableHttpResponse providerAisCityStateBatchValidate = prodReadinessTest.createHttpPostRequest(
                baseUrl,
                "/api/v2/address/citystate/batch?provider=uspsais", addressJson);
        jsonResponse = prodReadinessTest.getResponseFromInputStream(providerAisCityStateBatchValidate.getEntity().getContent());
        providerAisCityStateBatchValidate.close();
        prodReadinessTest.cityStateBatchResponseCheck(jsonResponse);

        /**
         * Geocode Batch Validation
         */
        CloseableHttpResponse standardGeocodeBatchValidate = prodReadinessTest.createHttpPostRequest(
                baseUrl,
                "/api/v2/geo/geocode/batch", addressJson);
        jsonResponse = prodReadinessTest.getResponseFromInputStream(standardGeocodeBatchValidate.getEntity().getContent());
        standardGeocodeBatchValidate.close();
        prodReadinessTest.batchSuccessResponseCheck(jsonResponse);

        CloseableHttpResponse providerGoogleGeocodeBatchValidate = prodReadinessTest.createHttpPostRequest(
                baseUrl,
                "/api/v2/geo/geocode/batch?provider=google", addressJson);
        jsonResponse = prodReadinessTest.getResponseFromInputStream(providerGoogleGeocodeBatchValidate.getEntity().getContent());
        providerGoogleGeocodeBatchValidate.close();
        prodReadinessTest.batchSuccessResponseCheck(jsonResponse);

        CloseableHttpResponse providerNYSGeoBatchValidate = prodReadinessTest.createHttpPostRequest(
                baseUrl,
                "/api/v2/geo/geocode/batch?provider=nysgeo", addressJson);
        jsonResponse = prodReadinessTest.getResponseFromInputStream(providerNYSGeoBatchValidate.getEntity().getContent());
        providerNYSGeoBatchValidate.close();
        prodReadinessTest.batchSuccessResponseCheck(jsonResponse);

        /**
         * RevGeocode Batch Validation
         */
        CloseableHttpResponse revGeocodeBatchValidate = prodReadinessTest.createHttpPostRequest(
                baseUrl,
                "/api/v2/geo/revgeocode/batch", pointJson);
        jsonResponse = prodReadinessTest.getResponseFromInputStream(revGeocodeBatchValidate.getEntity().getContent());
        revGeocodeBatchValidate.close();
        prodReadinessTest.batchSuccessResponseCheck(jsonResponse);

        /**
         * Dist Assign Batch Validation
         */
        CloseableHttpResponse standardDistAssignBatchValidate = prodReadinessTest.createHttpPostRequest(
                baseUrl,
                "/api/v2/district/assign/batch", addressJson);
        jsonResponse = prodReadinessTest.getResponseFromInputStream(standardDistAssignBatchValidate.getEntity().getContent());
        logger.info("DISTRICT ASSIGN BATCH: " + jsonResponse);
        standardDistAssignBatchValidate.close();
        prodReadinessTest.batchSuccessResponseCheck(jsonResponse);

        CloseableHttpResponse pointDistAssignBatchValidate = prodReadinessTest.createHttpPostRequest(
                baseUrl,
                "/api/v2/district/assign/batch", pointJson);
        jsonResponse = prodReadinessTest.getResponseFromInputStream(pointDistAssignBatchValidate.getEntity().getContent());
        pointDistAssignBatchValidate.close();
        prodReadinessTest.batchSuccessResponseCheck(jsonResponse);

        /**
         * Bluebird Batch Validation
         */
        CloseableHttpResponse standardBluebirdBatchValidate = prodReadinessTest.createHttpPostRequest(
                baseUrl,
                "/api/v2/district/bluebird/batch", addressJson);
        jsonResponse = prodReadinessTest.getResponseFromInputStream(standardBluebirdBatchValidate.getEntity().getContent());
        logger.info("BLUBIRD BATCH 1: " + jsonResponse);
        standardBluebirdBatchValidate.close();
        prodReadinessTest.batchSuccessResponseCheck(jsonResponse);

        CloseableHttpResponse shapeFallBackBluebirdBatchValidate = prodReadinessTest.createHttpPostRequest(
                baseUrl,
                "/api/v2/district/bluebird/batch?provider=streetfile&uspsValidate=true&districtStrategy=shapeFallBack", addressJson);
        jsonResponse = prodReadinessTest.getResponseFromInputStream(shapeFallBackBluebirdBatchValidate.getEntity().getContent());
        logger.info("BLUBIRD BATCH 2: " + jsonResponse);
        shapeFallBackBluebirdBatchValidate.close();
        prodReadinessTest.batchSuccessResponseCheck(jsonResponse);

        CloseableHttpResponse pointBluebirdBatchValidate = prodReadinessTest.createHttpPostRequest(
                baseUrl,
                "/api/v2/district/bluebird/batch", pointJson);
        jsonResponse = prodReadinessTest.getResponseFromInputStream(pointBluebirdBatchValidate.getEntity().getContent());
        pointBluebirdBatchValidate.close();
        prodReadinessTest.batchSuccessResponseCheck(jsonResponse);
    }
}
