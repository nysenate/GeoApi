package gov.nysenate.sage.provider.geocode;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.service.geo.GeocodeServiceValidator;
import gov.nysenate.sage.util.UrlRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class RubyGeocoder implements GeocodeService {
    private static final String DEFAULT_BASE_URL = "http://geocoder.nysenate.gov/GeoRubyAdapter/api";
    private final ObjectMapper jsonMapper;
    private final Logger logger;
    private final int BATCH_SIZE = 24;
    private final String m_baseUrl;
    private final String m_baseBulkUrl;
    private final GeocodeServiceValidator geocodeServiceValidator;


    @Autowired
    public RubyGeocoder(GeocodeServiceValidator geocodeServiceValidator) {
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.jsonMapper = new ObjectMapper();
        m_baseUrl = DEFAULT_BASE_URL+"/geocode";
        m_baseBulkUrl = DEFAULT_BASE_URL+"/bulk";
        logger.debug("Initialized RubyGeocoder Adapter");
        this.geocodeServiceValidator = geocodeServiceValidator;
    }

    /** {@inheritDoc} */
    @Override
    public GeocodeResult geocode(Address address) {
        String url;
        GeocodeResult geocodeResult = new GeocodeResult(this.getClass());

        /** Ensure that the geocoder is active, otherwise return error result. */
        if (!geocodeServiceValidator.isGeocodeServiceActive(this.getClass(), geocodeResult)) {
            return geocodeResult;
        }

        /** Proceed if valid address */
        if (!GeocodeServiceValidator.validateGeocodeInput(address, geocodeResult)){
            return geocodeResult;
        }

        url = m_baseUrl+"?street="+address.getAddr1()+"&city="+address.getPostalCity()+"&state="+address.getState()+"&zip="+address.getZip5();
        url = url.replaceAll(" ", "%20");

        try {
            logger.info(url);
            String jsonString = UrlRequest.getResponseFromUrl(url);
            if (jsonString != null) {
                logger.info(jsonString);
                JsonNode root = this.jsonMapper.readTree(jsonString);

                if (root == null) {
                    geocodeResult = new GeocodeResult(this.getClass(), ResultStatus.NO_GEOCODE_RESULT);
                    return geocodeResult;
                }

                JsonNode jsonResult = root.get(0);
                geocodeResult = getGeocodeResultFromResultNode(jsonResult);
            }
            else {
                geocodeResult.setStatusCode(ResultStatus.RESPONSE_ERROR);
            }
        }
        catch (IOException e) {
            logger.error("Error opening API resource '"+url+"'", e);
        }
        return geocodeResult;
    }

    /** {@inheritDoc} */
    @Override
    public List<GeocodeResult> geocode(List<Address> addresses) {
        // RubyGeocoder has a special bulk method if all addresses are parsed
        if (addresses.isEmpty()) {
            return List.of();
        }

        String urlText = "";
        ArrayList<GeocodeResult> results = new ArrayList<>();
        ArrayList<GeocodeResult> batchResults = new ArrayList<>();

        try {
            var json = new StringBuilder();
            // Start with a=1 to make the batch boundary condition work nicely
            for (int a = 1; a <= addresses.size(); a++) {
                Address address = addresses.get(a-1);
                if (address == null) {
                    batchResults.add(null);
                }
                else {
                    batchResults.add(new GeocodeResult(this.getClass()));
                    json.append(",");
                    json.append(addressToJson(address));
                }

                // Stop here unless we've filled this batch request
                if (a % BATCH_SIZE != 0 && a != addresses.size()) {
                    continue;
                }

                urlText = m_baseBulkUrl+"?json=["+ URLEncoder.encode(json.substring(1), StandardCharsets.UTF_8)+"]";
                logger.info(urlText);

                String jsonString = UrlRequest.getResponseFromUrl(urlText);
                logger.info(jsonString);


                // Each address specified produces its own result node.
                // Because null addresses aren't sent to MapQuest we need
                // to track an offset to the corresponding result.
                int resultOffset = 0;
                JsonNode jsonResults = this.jsonMapper.readTree("[" + jsonString + "]");

                for (int i = 0; i < jsonResults.size(); i++) {
                    JsonNode jsonResult = jsonResults.get(i);
                    while (batchResults.get(i+resultOffset) == null) {
                        logger.info("looping! {}", resultOffset);
                        resultOffset++;
                    }
                    batchResults.set(i+resultOffset, getGeocodeResultFromResultNode(jsonResult));
                }

                json = new StringBuilder();
                results.addAll(batchResults);
                batchResults.clear();
            }
            return results;
        }
        catch (IOException e) {
            String msg = "Error opening API resource '"+urlText+"'";
            logger.error(msg, e);
        }
        return results;
    }

    private GeocodeResult getGeocodeResultFromResultNode(JsonNode jsonResult) {
        GeocodeResult geocodeResult = new GeocodeResult();
        if (jsonResult != null && jsonResult.has("lat")) {
            // For lower granularity lookups these fields might not be available.
            String street = "";
            if (jsonResult.has("prenum"))
                street += jsonResult.get("prenum").asText() +" ";
            if (jsonResult.has("number"))
                street += jsonResult.get("number").asText() +" ";
            if (jsonResult.has("street"))
                street += jsonResult.get("street").asText();
            street = street.trim();
            String city = jsonResult.has("city") ? jsonResult.get("city").asText() : "";
            String state = jsonResult.has("state") ? jsonResult.get("state").asText() : "";
            String zip = jsonResult.has("zip") ? jsonResult.get("zip").asText() : "";
            double lat = jsonResult.has("lat") ? jsonResult.get("lat").asDouble() : 0;
            double lon = jsonResult.has("lon") ? jsonResult.get("lon").asDouble() : 0;
            int quality = jsonResult.has("score") ? (int)(jsonResult.get("score").asDouble() *100) : 0;
            if (quality == 100) {
                quality = 99; // No geocode is perfect
            }
            Address resultAddress = new Address(street, city, state, zip);
            Geocode geocode = new Geocode(new Point(lat, lon), GeocodeQuality.UNKNOWN, this.getClass().getSimpleName());
            geocode.setRawQuality(quality);
            GeocodedAddress geocodedAddress = new GeocodedAddress(resultAddress, geocode);
            geocodeResult.setGeocodedAddress(geocodedAddress);
        }
        else {
            geocodeResult.setStatusCode(ResultStatus.NO_GEOCODE_RESULT);
        }
        return geocodeResult;
    }

    private static String addressToJson(Address address) {
        return String.format("{\"street\":\"%s\",\"city\":\"%s\",\"state\":\"%s\",\"zip5\":\"%s\"}",
                address.getAddr1(), address.getPostalCity(), address.getState(), address.getZip5());
    }

}

