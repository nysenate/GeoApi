package gov.nysenate.sage.dao.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.result.AddressResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.util.Config;
import gov.nysenate.sage.util.FormatUtil;
import gov.nysenate.sage.util.TimeUtil;
import gov.nysenate.sage.util.UrlRequest;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Data abstraction layer for querying the USPS AMS web service to perform address and city/state
 * lookups.
 */
public class USPSAMSDao implements Observer
{
    private static final Config config = ApplicationFactory.getConfig();
    private static String DEFAULT_BASE_URL = "";
    private static String VALIDATE_METHOD = "validate";
    private static String CITYSTATE_METHOD = "citystate";

    private Logger logger = Logger.getLogger(this.getClass());
    private static ObjectMapper objectMapper = new ObjectMapper();

    public USPSAMSDao()
    {
        this.update(null, null);
        config.notifyOnChange(this);
    }

    @Override
    public void update(Observable o, Object arg)
    {
        DEFAULT_BASE_URL = config.getValue("usps.ams.api.url");
    }

    /**
     * Performs a single address validation request.
     * @param address Input Address
     * @return AddressResult
     */
    public AddressResult getValidatedAddressResult(Address address)
    {
        if (address != null) {
            StringBuilder urlParams = new StringBuilder();

            try {
                urlParams.append("?addr1=" + URLEncoder.encode(address.getAddr1(), "UTF-8"));
                urlParams.append("&addr2=" + URLEncoder.encode(address.getAddr2(), "UTF-8"));
                urlParams.append("&city=" + URLEncoder.encode(address.getCity(), "UTF-8"));
                urlParams.append("&state=" + URLEncoder.encode(address.getState(), "UTF-8"));
                urlParams.append("&zip5=" + URLEncoder.encode(address.getZip5(), "UTF-8"));
                urlParams.append("&initCaps=true");

                String url = DEFAULT_BASE_URL + VALIDATE_METHOD + urlParams.toString();
                String response = UrlRequest.getResponseFromUrl(url);
                if (response != null && !response.isEmpty()) {
                    JsonNode root = objectMapper.readTree(response);
                    AddressResult addressResult = getAddressResultFromJsonValidate(root);
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
        }
        return null;
    }

    /**
     * Performs batch address validation request.
     * @param addresses List of Addresses to validate.
     * @return List<AddressResult>
     */
    public List<AddressResult> getValidatedAddressResults(List<Address> addresses)
    {
        List<AddressResult> addressResults = new ArrayList<>();
        if (addresses != null) {
            JsonNodeFactory jsonNodeFactory = JsonNodeFactory.instance;
            ArrayNode requestRoot = jsonNodeFactory.arrayNode();
            for (Address address : addresses) {
                ObjectNode addressNode = jsonNodeFactory.objectNode();
                addressNode.put("addr1", address.getAddr1());
                addressNode.put("addr2", address.getAddr2());
                addressNode.put("city", address.getCity());
                addressNode.put("state", address.getState());
                addressNode.put("zip5", address.getZip5());
                addressNode.put("zip4", address.getZip4());
                requestRoot.add(addressNode);
            }
            String jsonPayload = requestRoot.toString();
            String url = DEFAULT_BASE_URL + VALIDATE_METHOD + "?batch=true&initCaps=true";
            try {
                String json = UrlRequest.getResponseFromUrlUsingPOST(url, jsonPayload);
                if (json != null && !json.isEmpty()) {
                    JsonNode responseRoot = objectMapper.readTree(json);
                    if (responseRoot != null) {
                        int total = responseRoot.get("total").asInt();
                        JsonNode resultsNode = responseRoot.get("results");
                        for (int i = 0; i < total; i++) {
                            JsonNode resultNode = resultsNode.get(i);
                            addressResults.add(getAddressResultFromJsonValidate(resultNode));
                        }
                    }
                }
            }
            catch (IOException ex) {
                logger.error("Failed to get and parse response from batch validate request!", ex);
            }
        }
        return addressResults;
    }

    /**
     * Parses the USPS AMS Web service JSON response returned when calling the validate API.
     * @param root The JsonNode representing the top level node of the response.
     * @throws IOException
     */
    private AddressResult getAddressResultFromJsonValidate(JsonNode root) throws IOException
    {
        if (root != null) {
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
                String addr1 = addressNode.get("addr1").asText();
                String addr2 = addressNode.get("addr2").asText();
                String city = addressNode.get("city").asText();
                String state = addressNode.get("state").asText();
                String zip5 = addressNode.get("zip5").asText();
                String zip4 = addressNode.get("zip4").asText();

                Address validatedAddress = new Address(addr1, addr2, city, state, zip5, zip4);
                validatedAddress.setUspsValidated(true);

                addressResult.setAddress(validatedAddress);
            }
            else {
                addressResult.setStatusCode(ResultStatus.NO_ADDRESS_VALIDATE_RESULT);
            }
            addressResult.setResultTime(TimeUtil.currentTimestamp());
            return addressResult;
        }
        return null;
    }



     public AddressResult getCityStateResult(Address address)
     {
         if (address != null && address.getZip5() != null)
         {
             StringBuilder urlParams = new StringBuilder();
             urlParams.append("?initCaps=true");

             try
             {
                 urlParams.append("&zip5=" + URLEncoder.encode(address.getZip5(), "UTF-8"));
                 String url = DEFAULT_BASE_URL + CITYSTATE_METHOD + urlParams.toString();
                 String response = UrlRequest.getResponseFromUrl(url);
                 if (response != null && !response.isEmpty()) {
                     JsonNode root = objectMapper.readTree(response);
                     AddressResult addressResult = getAddressResultFromJsonCityState(root);
                     return addressResult;
                 }
                 else
                 {
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
         }
         return null;
     }

    public List<AddressResult> getCityStateResults (List<Address> addresses)
    {
        List<AddressResult> addressResults = new ArrayList<>();
        if (addresses != null)
        {
            JsonNodeFactory jsonNodeFactory = JsonNodeFactory.instance;
            ArrayNode requestRoot = jsonNodeFactory.arrayNode();
            for (Address address : addresses) {
                if(address.getZip5() != null)
                {
                    ObjectNode addressNode = jsonNodeFactory.objectNode();
                    addressNode.put("zip5", address.getZip5());
                    requestRoot.add(addressNode);
                }
            }
                String jsonPayload = requestRoot.toString();
                String url = DEFAULT_BASE_URL + CITYSTATE_METHOD + "?batch=true&initCaps=true";
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
        }
        return addressResults;
    }

    private AddressResult getAddressResultFromJsonCityState(JsonNode root) throws IOException
    {
        if (root != null) {
            AddressResult addressResult = new AddressResult();
            boolean success = root.get("success").asBoolean(false);
            addressResult.setValidated(success);
            if(success)
            {
                Address cityState = new Address(null, null, root.get("cityName").asText(),
                        root.get("stateAbbr").asText(),  root.get("zipCode").asText(), null);
                cityState.setUspsValidated(true);

                addressResult.setAddress(cityState);
            }
            else
            {
                addressResult.setValidated(false);
            }
            addressResult.setResultTime(TimeUtil.currentTimestamp());
            return addressResult;
        }
        return null;
    }
}
