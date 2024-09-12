package gov.nysenate.sage.dao.provider.usps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.result.AddressResult;
import gov.nysenate.sage.util.StreetAddressParser;
import gov.nysenate.sage.util.TimeUtil;
import gov.nysenate.sage.util.UrlRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static gov.nysenate.sage.model.result.ResultStatus.NO_ADDRESS_VALIDATE_RESULT;

/**
 * Data abstraction layer for querying the USPS AMS web service to perform address and city/state
 * lookups.
 */
@Repository
public class HttpUSPSAMSDao implements USPSAMSDao {
    private static final Logger logger = LoggerFactory.getLogger(HttpUSPSAMSDao.class);
    private static final String VALIDATE_METHOD = "validate";
    private static final String CITYSTATE_METHOD = "citystate";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final String base_url;

    @Autowired
    public HttpUSPSAMSDao(Environment env) {
        this.base_url = env.getUspsAmsApiUrl();
    }

    /** {@inheritDoc} */
    public AddressResult getValidatedAddressResult(Address address) {
        if (address == null) {
            return null;
        }

        StringBuilder urlParams = new StringBuilder();
        try {
            urlParams.append("?addr1=").append(encode(address.getAddr1()))
                    .append("&addr2=").append(encode(address.getAddr2()))
                    .append("&city=").append(encode(address.getPostalCity()))
                    .append("&state=").append(encode(address.getState()))
                    .append("&zip5=").append(address.getZip5())
                    .append("&initCaps=true");

            String url = base_url + VALIDATE_METHOD + urlParams;
            logger.info("Making a connection to: \n{}", url);

            String response = UrlRequest.getResponseFromUrl(url);
            if (response != null && !response.isEmpty()) {
                JsonNode root = objectMapper.readTree(response);
                AddressResult addressResult = getAddressResultFromJsonValidate(root);
                if (addressResult.getAddress() != null) {
                    addressResult.setAddress(
                            StreetAddressParser.performInitCapsOnAddress(
                                    StreetAddressParser.parseAddress(addressResult.getAddress()).toAddress()));
                }
                else { //This is what would happen if it was null but this prevents a null pointer exception
                    addressResult.setAddress(new Address());
                }
                return addressResult;
            }
            else {
                logger.error("Failed to obtain a valid response from USPS AMS!");
            }
        }
        catch (UnsupportedEncodingException ex) {
            logger.error("Failed to encode URL in UTF-8!", ex);
        }
        catch (IOException ex) {
            logger.error("Failed to obtain response!", ex);
        }
        catch (Exception ex) {
            logger.error("Failed to parse response!", ex);
        }
        return null;
    }

    /** {@inheritDoc} */
    public List<AddressResult> getValidatedAddressResults(List<Address> addresses) {
        if (addresses == null) {
            return List.of();
        }
        JsonNodeFactory jsonNodeFactory = JsonNodeFactory.instance;
        ArrayNode requestRoot = jsonNodeFactory.arrayNode();
        for (Address address : addresses) {
            ObjectNode addressNode = jsonNodeFactory.objectNode()
                    .put("addr1", address.getAddr1())
                    .put("addr2", address.getAddr2())
                    .put("city", address.getPostalCity())
                    .put("state", address.getState())
                    .put("zip5", address.getZip5())
                    .put("zip4", address.getZip4());
            requestRoot.add(addressNode);
        }
        String jsonPayload = requestRoot.toString();
        String url = base_url + VALIDATE_METHOD + "?batch=true&initCaps=true";
        try {
            String json = UrlRequest.getResponseFromUrlUsingPOST(url, jsonPayload);
            var addressResults = new ArrayList<AddressResult>();
            JsonNode responseRoot = objectMapper.readTree(json);
            for (JsonNode node : responseRoot.get("results")) {
                addressResults.add(getAddressResultFromJsonValidate(node));
            }
            return addressResults;
        }
        catch (IOException | IllegalArgumentException ex) {
            logger.error("Failed to get and parse response from batch validate request!", ex);
            return List.of();
        }

    }

    /**
     * Parses the USPS AMS Web service JSON response returned when calling the validate API.
     * @param root The JsonNode representing the top level node of the response.
     */
    private AddressResult getAddressResultFromJsonValidate(JsonNode root) {
        if (root == null) {
            return null;
        }
        AddressResult addressResult = new AddressResult();
        JsonNode statusNode = root.get("status");
        JsonNode addressNode = root.get("address");
        JsonNode footnotesNode = root.get("footnotes");

        boolean validated = root.get("validated").asBoolean(false);
        String status = statusNode.get("name").asText();

        addressResult.setValidated(validated);
        addressResult.addMessage(String.format("Status: %s", status));

        for (int i = 0; i < footnotesNode.size(); i++) {
            JsonNode footnoteNode = footnotesNode.get(i);
            String ftName = footnoteNode.get("name").asText();
            String ftDesc = footnoteNode.get("desc").asText();
            addressResult.addMessage(String.format("%s - %s", ftName, ftDesc));
        }

        if (validated) {
            try {
                String addr1 = addressNode.get("addr1").asText();
                String addr2 = addressNode.get("addr2").asText();
                String city = addressNode.get("city").asText();
                String state = addressNode.get("state").asText();
                String zip5 = addressNode.get("zip5").asText();
                String zip4 = addressNode.get("zip4").asText();

                Address validatedAddress = new Address(addr1, addr2, city, state, zip5, zip4);
                validatedAddress.setUspsValidated(true);

                addressResult.setAddress(validatedAddress);
            } catch (NullPointerException ex) {
                logger.error("Bad root: {}", root);
                addressResult.setValidated(false);
            }
        }
        else {
            addressResult.setStatusCode(NO_ADDRESS_VALIDATE_RESULT);
        }
        addressResult.setResultTime(TimeUtil.currentTimestamp());
        return addressResult;
    }

    public AddressResult getCityStateResult(Address address) {
        if (address == null || address.getZip5() == null) {
            return null;
        }

        StringBuilder urlParams = new StringBuilder("?initCaps=true");
        try {
            urlParams.append("&zip5=").append(address.getZip5());
            String url = base_url + CITYSTATE_METHOD + urlParams;
            String response = UrlRequest.getResponseFromUrl(url);
            if (response != null && !response.isEmpty()) {
                JsonNode root = objectMapper.readTree(response);
                return getAddressResultFromJsonCityState(root);
            }
            else {
                logger.error("Failed to obtain a valid response from USPS AMS!");
            }

        }
        catch (UnsupportedEncodingException ex) {
            logger.error("Failed to encode URL in UTF-8!", ex);
        }
        catch (IOException ex) {
            logger.error("Failed to obtain response!", ex);
        }
        catch (Exception ex) {
            logger.error("Failed to parse response!", ex);
        }
        return null;
    }

    public List<AddressResult> getCityStateResults (List<Address> addresses) {
        List<AddressResult> addressResults = new ArrayList<>();
        if (addresses == null) {
            return addressResults;
        }
        var zip5List = addresses.stream().filter(Objects::nonNull)
                .map(Address::getZip5).collect(Collectors.toList());
        Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();

        String jsonPayload = prettyGson.toJson(zip5List);
        String url = base_url + CITYSTATE_METHOD + "?batch=true&initCaps=true";
        try {
            String json = UrlRequest.getResponseFromUrlUsingPOST(url, jsonPayload);
            if (json != null && !json.isEmpty()) {
                JsonNode responseRoot = objectMapper.readTree(json);
                if (responseRoot != null) {
                    int total = responseRoot.get("total").asInt();
                    JsonNode resultsNode = responseRoot.get("results");
                    for (int i = 0; i < total; i++) {
                        JsonNode resultNode = resultsNode.get(i);
                        addressResults.add(getAddressResultFromJsonCityState(resultNode));
                    }
                }
            }
        }
        catch (IOException ex) {
            logger.error("Failed to get and parse response from batch validate request!", ex);
        }
        return addressResults;
    }

    private AddressResult getAddressResultFromJsonCityState(JsonNode root) throws IOException {
        if (root == null) {
            return null;
        }
        AddressResult addressResult = new AddressResult();
        boolean success = root.get("success").asBoolean(false);
        addressResult.setValidated(success);
        if (success) {
            Address cityState = new Address(null, null, root.get("cityName").asText(),
                    root.get("stateAbbr").asText(),  root.get("zipCode").asText(), null);
            cityState.setUspsValidated(true);
            addressResult.setAddress(cityState);
        }
        else {
            addressResult.setValidated(false);
            addressResult.setStatusCode(NO_ADDRESS_VALIDATE_RESULT);
        }
        addressResult.setResultTime(TimeUtil.currentTimestamp());
        return addressResult;
    }

    private static String encode(String input) {
        return URLEncoder.encode(input, StandardCharsets.UTF_8);
    }
}
