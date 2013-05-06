package gov.nysenate.sage.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.service.geo.GeocodeService;
import gov.nysenate.sage.util.Config;
import gov.nysenate.sage.util.UrlRequest;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class RubyGeocoder implements GeocodeService
{
    private static final String DEFAULT_BASE_URL = "http://geocoder.nysenate.gov/GeoRubyAdapter/api";
    private final ObjectMapper jsonMapper;
    private final Logger logger;
    private final Config config;
    private final int BATCH_SIZE = 24;
    private String m_baseUrl;
    private String m_baseBulkUrl;

    public RubyGeocoder()
    {
        this.config = ApplicationFactory.getConfig();
        this.logger = Logger.getLogger(this.getClass());
        this.jsonMapper = new ObjectMapper();
        configure();
        logger.info("Initialized RubyGeocoder Adapter");
    }

    private void configure()
    {
        m_baseUrl = DEFAULT_BASE_URL+"/geocode";
        m_baseBulkUrl = DEFAULT_BASE_URL+"/bulk";
    }

    @Override
    public GeocodeResult geocode(Address address)
    {
        if (address == null) {
            return null;
        }

        String url;
        GeocodeResult geocodeResult = null;
        if (address.isParsed()) {
            url = m_baseUrl+"?street="+address.getAddr1()+"&city="+address.getCity()+"&state="+address.getState()+"&zip="+address.getZip5();
        }
        else {
            url = m_baseUrl+"?address="+address.toString();
        }
        url = url.replaceAll(" ", "%20");

        try {
            logger.info(url);
            String jsonString = UrlRequest.getResponseFromUrl(url);
            logger.info(jsonString);
            JsonNode root = this.jsonMapper.readTree(jsonString);

            if (root == null) {
                geocodeResult = new GeocodeResult(this.getClass(), ResultStatus.NO_GEOCODE_RESULT);
                return geocodeResult;
            }

            JsonNode jsonResult = root.get(0);
            geocodeResult = getGeocodeResultFromResultNode(jsonResult);
            return geocodeResult;
        }
        catch (IOException e) {
            logger.error("Error opening API resource '"+url+"'", e);
            return null;
        }
    }

    @Override
    public ArrayList<GeocodeResult> geocode(ArrayList<Address> addresses)
    {
        // RubyGeocoder has a special bulk method if all addresses are parsed
        return geocodeParsedBulk(addresses);
    }

    @Override
    public GeocodeResult reverseGeocode(Point point) {
        return null;
    }

    @Override
    public ArrayList<GeocodeResult> reverseGeocode(ArrayList<Point> points) {
        return null;
    }

    private ArrayList<GeocodeResult> geocodeParsedBulk(ArrayList<Address> addresses)
    {
        String urlText = "";
        ArrayList<GeocodeResult> results = new ArrayList<>();
        ArrayList<GeocodeResult> batchResults = new ArrayList<>();

        if (addresses.size() == 0) {
            return results;
        }

        try {
            StringBuilder json = new StringBuilder();
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

                urlText = m_baseBulkUrl+"?json=["+ URLEncoder.encode(json.substring(1), "utf-8")+"]";
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
                        logger.info(("looping! " + resultOffset));
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
        catch (UnsupportedEncodingException e) {
            String msg = "UTF-8 encoding not supported!?";
            logger.error(msg);
        }
        catch (IOException e) {
            String msg = "Error opening API resource '"+urlText+"'";
            logger.error(msg, e);
        }
        return results;
    }

    private GeocodeResult getGeocodeResultFromResultNode(JsonNode jsonResult)
    {
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

    private String addressToJson(Address address)
    {
        return String.format("{\"street\":\"%s\",\"city\":\"%s\",\"state\":\"%s\",\"zip5\":\"%s\"}",
                address.getAddr1(), address.getCity(), address.getState(), address.getZip5());
    }

}

