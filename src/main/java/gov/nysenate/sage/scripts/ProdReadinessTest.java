package gov.nysenate.sage.scripts;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.geo.Point;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class ProdReadinessTest {
    private static final Logger logger = LoggerFactory.getLogger(ProdReadinessTest.class);
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    @Getter
    private String baseUrl = "http://localhost:8080";

    Properties prop =  new Properties();
    private final List<Address> testAddresses = List.of(
            new Address("100 Nyroy Dr", "Troy", "NY", "12180"),
            new Address("44 Fairlawn Ave","Albany","NY","12203"),
            new Address("903 London Square Drive","Clifton Park","NY","12065"),
            new Address("535 Highland Ave","Rochester","NY","14620"),
            new Address("46-08 74th Street","Flushing","NY","11373"),
            new Address("200 State Street","Albany","NY","12210")
    );

    private static final List<Address> badTestAddresses = List.of(
            new Address("","Albany","NY","12205"),
            new Address("25 Smithtown Circle","Smithtown", "NY","11787"),
            new Address("25 Smithtown","Smithtown", "NY","11787")
    );

    private final ArrayList<Point> testPoints = new ArrayList<>();

    private void initializeProperties() throws IOException, NullPointerException {
        InputStream appPropsStream = getClass().getResourceAsStream("/app.properties");
        this.prop.load(appPropsStream);
        this.baseUrl = this.prop.getProperty("base.url");
    }
    
    private void initializeTestPoints() { //These correspond directly to the addresses in the testAddresses array
        testPoints.add(new Point("42.7410467", "-73.6691371"));
        testPoints.add(new Point("42.6711474", "-73.79940049999999"));
        testPoints.add(new Point("42.8666825", "-73.8010151"));
        testPoints.add(new Point("40.8677979", "-73.83986170000001"));
        testPoints.add(new Point("43.13010999999999", "-77.5993111"));
        testPoints.add(new Point("40.7397789", "-73.88993359999999"));
        testPoints.add(new Point("42.6533668", "-73.7599828"));
    }

    private JsonNode sendGetRequest(String ctxPath, String apiPath) throws IOException, InterruptedException {
        return send(requestBuilder(ctxPath, apiPath).GET());
    }

    private JsonNode sendPostRequest(String ctxPath, String apiPath, String json) throws IOException, InterruptedException {
        return send(requestBuilder(ctxPath, apiPath)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json)));
    }

    private static HttpRequest.Builder requestBuilder(String ctxPath, String apiPath) {
        apiPath = apiPath.replace(" ","%20");
        logger.info("{}{}\n", ctxPath, apiPath);
        return HttpRequest.newBuilder(URI.create(ctxPath + apiPath))
                .header("Accept", "application/json");
    }

    private static JsonNode send(HttpRequest.Builder request) throws IOException, InterruptedException {
        HttpResponse<String> response = httpClient.send(request.build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new IOException("HTTP " + response.statusCode() + " from " + response.uri());
        }
        return objectMapper.readTree(response.body());
    }

    private String turnAddressesIntoJson() {
        return convertObjToJson(testAddresses);
    }

    private String turnBadAddressesIntoJson() {
        return convertObjToJson(badTestAddresses);
    }

    private String turnPointsIntoJson() {
        return convertObjToJson(testPoints);
    }

    private String convertObjToJson(List<?> list) {
        Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
        return prettyGson.toJson(list);
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
        try {
            prodReadinessTest.initializeProperties();
        }
        catch (IOException | NullPointerException e) {
            logger.warn("Unable to initialize properties", e);
        }
        prodReadinessTest.initializeTestPoints();

        String baseUrl = prodReadinessTest.getBaseUrl();
        JsonNode jsonResponse;
        String addressJson = prodReadinessTest.turnAddressesIntoJson();
        String badAddressJson = prodReadinessTest.turnBadAddressesIntoJson();
        String pointJson = prodReadinessTest.turnPointsIntoJson();

        // Test Address Api Functionality
        jsonResponse = prodReadinessTest.sendGetRequest(
               baseUrl, "/api/v2/address/validate?addr1=44 Fairlawn Avenue&city=Albany&state=NY");
        assertEquals(0, prodReadinessTest.standardSuccessResponseCheck(jsonResponse)); //Expected - Actual

        jsonResponse = prodReadinessTest.sendGetRequest(
                baseUrl, "/api/v2/address/validate?addr1=44 Fairlawn Avenue&city=Albany&state=NY&provider=AIS");
        assertEquals(0, prodReadinessTest.standardSuccessResponseCheck(jsonResponse));

        jsonResponse = prodReadinessTest.sendGetRequest(
                baseUrl,
                "/api/v2/address/citystate?zip5=12210");
        assertEquals(0, prodReadinessTest.standardSuccessResponseCheck(jsonResponse));

        jsonResponse = prodReadinessTest.sendGetRequest(
                baseUrl,
                "/api/v2/address/citystate?zip5=12210&provider=AIS");
        assertEquals(0, prodReadinessTest.standardSuccessResponseCheck(jsonResponse));


        // Test Geo Api Functionality
        jsonResponse = prodReadinessTest.sendGetRequest(
                baseUrl,
                "/api/v2/geo/geocode?addr=200 State St, Albany NY 12210");
        assertEquals(0, prodReadinessTest.standardSuccessResponseCheck(jsonResponse));


        jsonResponse = prodReadinessTest.sendGetRequest(
                baseUrl,
                "/api/v2/geo/geocode?addr1=200 State St&city=Albany&state=NY&zip5=12210");
        assertEquals(0, prodReadinessTest.standardSuccessResponseCheck(jsonResponse));


        jsonResponse = prodReadinessTest.sendGetRequest(
                baseUrl,
                "/api/v2/geo/geocode?addr1=200 State St&city=Albany&state=NY&zip5=12210&geocoder=GEOCACHE");
        assertEquals(0, prodReadinessTest.standardSuccessResponseCheck(jsonResponse));


        jsonResponse = prodReadinessTest.sendGetRequest(
                baseUrl,
                "/api/v2/geo/geocode?addr1=200 State St&city=Albany&state=NY&zip5=12210&geocoder=GOOGLE");
        assertEquals(0, prodReadinessTest.standardSuccessResponseCheck(jsonResponse));


        jsonResponse = prodReadinessTest.sendGetRequest(
                baseUrl,
                "/api/v2/geo/geocode?addr1=200 State St&city=Albany&state=NY&zip5=12210&geocoder=NYSGEO");
        assertEquals(0, prodReadinessTest.standardSuccessResponseCheck(jsonResponse));


        jsonResponse = prodReadinessTest.sendGetRequest(
                baseUrl,
                "/api/v2/geo/revgeocode?lat=42.6533668&lon=-73.7621715");
        assertEquals(0, prodReadinessTest.standardSuccessResponseCheck(jsonResponse));


        // Test Street API
        jsonResponse = prodReadinessTest.sendGetRequest(
                baseUrl,
                "/api/v2/street/lookup?zip5=12210");
        assertEquals(0, prodReadinessTest.standardSuccessResponseCheck(jsonResponse));


        // Test Bluebird District Assignment API
        jsonResponse = prodReadinessTest.sendGetRequest(
                baseUrl,
                "/api/v2/district/bluebird?addr=280 Madison Ave New York NY");
        assertEquals(0, prodReadinessTest.standardSuccessResponseCheck(jsonResponse));


        jsonResponse = prodReadinessTest.sendGetRequest(
                baseUrl,
                "/api/v2/district/bluebird?addr1=280 Madison Ave New York&state=NY");
        assertEquals(0, prodReadinessTest.standardSuccessResponseCheck(jsonResponse));



        // Test Standard District Assignment
        jsonResponse = prodReadinessTest.sendGetRequest(
                baseUrl,
                "/api/v2/district/assign?addr=280 Madison Ave, New York, NY");
        assertEquals(0, prodReadinessTest.standardSuccessResponseCheck(jsonResponse));


        jsonResponse = prodReadinessTest.sendGetRequest(
                baseUrl,
                "/api/v2/district/assign?addr1=280 Madison Ave&city=New York&state=NY");
        assertEquals(0, prodReadinessTest.standardSuccessResponseCheck(jsonResponse));


        jsonResponse = prodReadinessTest.sendGetRequest(
                baseUrl,
                "/api/v2/district/assign?addr=200 State Street,Albany,NY,USA&uspsValidate=true&showMaps=true");
        assertEquals(0, prodReadinessTest.standardSuccessResponseCheck(jsonResponse));


        jsonResponse = prodReadinessTest.sendGetRequest(
                baseUrl,
                "/api/v2/district/assign?addr1=280 Madison Ave&city=New York&state=NY");
        assertEquals(0, prodReadinessTest.standardSuccessResponseCheck(jsonResponse));


        jsonResponse = prodReadinessTest.sendGetRequest(
                baseUrl,
                "/api/v2/district/assign?addr=200%20State%20Street,%20Albany,%20NY,%20USA&districtSource=STREETFILE&uspsValidate=true&showMaps=true");
        assertEquals(0, prodReadinessTest.standardSuccessResponseCheck(jsonResponse));


        jsonResponse = prodReadinessTest.sendGetRequest(
                baseUrl,
                "/api/v2/district/assign?addr=200%20State%20Street,%20Albany,%20NY,%20USA&districtSource=SHAPEFILE&uspsValidate=true&showMaps=true");
        assertEquals(0, prodReadinessTest.standardSuccessResponseCheck(jsonResponse));


        jsonResponse = prodReadinessTest.sendGetRequest(
                baseUrl,
                "/api/v2/district/assign?lat=40.751352&lon=-73.980335");
        assertEquals(0, prodReadinessTest.standardSuccessResponseCheck(jsonResponse));

        // Address Batch Validation
        jsonResponse = prodReadinessTest.sendPostRequest(
                baseUrl,
                "/api/v2/address/validate/batch", addressJson);
        logger.info("ADDRESS VALIDATION BATCH: {}", jsonResponse);
        prodReadinessTest.addressBatchValidateResponseCheck(jsonResponse);

        jsonResponse = prodReadinessTest.sendPostRequest(
                baseUrl,
                "/api/v2/address/validate/batch", badAddressJson);
        logger.info("BAD ADDRESS VALIDATION BATCH: {}", jsonResponse);
        prodReadinessTest.badAddressBatchValidateResponseCheck(jsonResponse);

        jsonResponse = prodReadinessTest.sendPostRequest(
                baseUrl,
                "/api/v2/address/validate/batch?provider=AIS", addressJson);
        prodReadinessTest.addressBatchValidateResponseCheck(jsonResponse);


        jsonResponse = prodReadinessTest.sendPostRequest(
                baseUrl,
                "/api/v2/address/citystate/batch", addressJson);
        prodReadinessTest.cityStateBatchResponseCheck(jsonResponse);

        jsonResponse = prodReadinessTest.sendPostRequest(
                baseUrl,
                "/api/v2/address/citystate/batch?provider=AIS", addressJson);
        prodReadinessTest.cityStateBatchResponseCheck(jsonResponse);

        // Geocode Batch Validation
        jsonResponse = prodReadinessTest.sendPostRequest(
                baseUrl,
                "/api/v2/geo/geocode/batch", addressJson);
        prodReadinessTest.batchSuccessResponseCheck(jsonResponse);

        jsonResponse = prodReadinessTest.sendPostRequest(
                baseUrl,
                "/api/v2/geo/geocode/batch?geocoder=GOOGLE", addressJson);
        prodReadinessTest.batchSuccessResponseCheck(jsonResponse);

        jsonResponse = prodReadinessTest.sendPostRequest(
                baseUrl,
                "/api/v2/geo/geocode/batch?geocoder=NYSGEO", addressJson);
        prodReadinessTest.batchSuccessResponseCheck(jsonResponse);

        // RevGeocode Batch Validation
        jsonResponse = prodReadinessTest.sendPostRequest(
                baseUrl,
                "/api/v2/geo/revgeocode/batch", pointJson);
        prodReadinessTest.batchSuccessResponseCheck(jsonResponse);

        // Dist Assign Batch Validation
        logger.info("ADDRESS JSON{}", addressJson);
        jsonResponse = prodReadinessTest.sendPostRequest(
                baseUrl,
                "/api/v2/district/assign/batch", addressJson);
        logger.info("DISTRICT ASSIGN BATCH: {}", jsonResponse);
        logger.info("JSON RESPONSE{}", jsonResponse);
        prodReadinessTest.batchSuccessResponseCheck(jsonResponse);

        jsonResponse = prodReadinessTest.sendPostRequest(
                baseUrl,
                "/api/v2/district/assign/batch", pointJson);
        prodReadinessTest.batchSuccessResponseCheck(jsonResponse);

        // Bluebird Batch Validation
        jsonResponse = prodReadinessTest.sendPostRequest(
                baseUrl,
                "/api/v2/district/bluebird/batch", addressJson);
        logger.info("BLUEBIRD BATCH: {}", jsonResponse);
        prodReadinessTest.batchSuccessResponseCheck(jsonResponse);

        jsonResponse = prodReadinessTest.sendPostRequest(
                baseUrl,
                "/api/v2/district/bluebird/batch?districtSource=STREETFILE&uspsValidate=true", addressJson);
        logger.info("BLUEBIRD BATCH 2: {}", jsonResponse);
        prodReadinessTest.batchSuccessResponseCheck(jsonResponse);

        jsonResponse = prodReadinessTest.sendPostRequest(
                baseUrl,
                "/api/v2/district/bluebird/batch", pointJson);
        prodReadinessTest.batchSuccessResponseCheck(jsonResponse);
    }
}
