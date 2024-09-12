package gov.nysenate.sage.dao.provider.mapquest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.util.UrlRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.*;

import static gov.nysenate.sage.model.geo.GeocodeQuality.*;

@Repository
public class HttpMapQuestDao implements MapQuestDao {
    private static final String DEFAULT_FORMAT = "&outFormat=json&thumbMaps=false&maxResults=1";
    private static final int BATCH_SIZE = 95;

    private static final Logger logger = LoggerFactory.getLogger(HttpMapQuestDao.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    @Value("${mapquest.geo.url:http://www.mapquestapi.com/geocoding/v1/batch}")
    private String geoUrl;
    @Value("${mapquest.rev.url:http://www.mapquestapi.com/geocoding/v1/reverse}")
    private String revGeoUrl;
    @Value("${mapquest.key:API key obtained from Mapquest}")
    private String key;

    /**
     * Map the quality codes to GeocodeQuality values.
     * Point Values Based on Yahoo's quality point scheme
     * <a href="http://developer.yahoo.com/geo/placefinder/guide/responses.html#address-quality">...</a>
     * and the MapQuest geocode quality codes
     * <a href="http://www.mapquestapi.com/geocoding/geocodequality.html">...</a>
     */
    private static final Map<String, GeocodeQuality> qualityMap = new HashMap<>();
    static {
        qualityMap.put("POINT", POINT);
        qualityMap.put("STREET", STREET);
        qualityMap.put("COUNTY", COUNTY);
        qualityMap.put("CITY", CITY);
        qualityMap.put("STATE", STATE);
        qualityMap.put("ZIP", ZIP);
        qualityMap.put("ADDRESS", HOUSE);
        qualityMap.put("INTERSECTION", STREET);
        qualityMap.put("COUNTRY", STATE);
        qualityMap.put("ZIP_EXTENDED", ZIP_EXT);
    }

    /** {@inheritDoc} */
    public String getKey() {
        return key;
    }

    /** {@inheritDoc} */
    public List<GeocodedAddress> getGeocodedAddresses(List<Address> addresses) {
        /** Pre-populate the batch result with addresses */
        List<GeocodedAddress> geocodedAddresses = new ArrayList<>();
        for (Address address : addresses) {
            geocodedAddresses.add(new GeocodedAddress(address));
        }

        String baseUrl = geoUrl + "?key=" + getKey() + DEFAULT_FORMAT;
        StringBuilder locations = new StringBuilder();
        int batchCount = 0;
        int batchOffset = 0;
        for (int i = 1; i <= addresses.size(); i++) {
            try {
                Address address = addresses.get(i-1);
                locations.append((address == null) ? String.format("&location=%s", "null")
                        : String.format("&location=%s", URLEncoder.encode(address.toString(), "UTF-8")));
                batchCount++;
                if (i % BATCH_SIZE != 0 && i != addresses.size()) continue;

                /** A successful response will have the same size */
                List<GeocodedAddress> batchResults = getGeocodedAddresses(baseUrl + locations);
                if (batchResults.size() == batchCount) {
                    for (int j = 0; j < batchCount; j++) {
                        if (batchResults.get(j) != null && batchResults.get(j).isValidGeocode()) {
                            geocodedAddresses.set(j + batchOffset, batchResults.get(j));
                        }
                    }
                }
                else {
                    logger.warn("Skipping failed MapQuest batch ({} - {})", batchOffset, batchOffset + batchCount);
                }

                /** Reset batch counters */
                batchOffset += batchCount;
                batchCount = 0;
                locations = new StringBuilder();
            }
            catch (UnsupportedEncodingException ex){
                logger.error(MarkerFactory.getMarker("FATAL"), "{}", String.valueOf(ex));
            }
        }
        return geocodedAddresses;
    }

    /** {@inheritDoc} */
    public GeocodedAddress getGeocodedAddress(Point point) {
        String url = revGeoUrl + "?key=" + getKey() + DEFAULT_FORMAT + String.format("&lat=%f&lng=%f", point.getLat(), point.getLon());
        List<GeocodedAddress> revGeocodedAddresses = getGeocodedAddresses(url);

        if (revGeocodedAddresses != null && !revGeocodedAddresses.isEmpty()){
            return revGeocodedAddresses.get(0);
        }
        return null;
    }

    /**
     * Abstraction method that makes a request to the given MapQuest url, parses
     * the response, and returns a list of GeocodedAddresses.
     * @param url   The query url
     *              e.g. <a href="http://www.mapquestapi.com/geocoding/v1/address?..blah">...</a>
     * @return GeocodedAddress, or null on parse failure
     */
    private static List<GeocodedAddress> getGeocodedAddresses(String url) {
        List<GeocodedAddress> geocodedAddresses = new ArrayList<>();
        String json = "";
        try {
            /** Format and send request */
            json = UrlRequest.getResponseFromUrl(url);
            if (json != null) {
                JsonNode jsonNode = objectMapper.readTree(json);

                /** Error checking */
                String status = jsonNode.get("info").get("statuscode").asText();
                if (!Objects.equals(status, "0")) {
                    logger.debug("MapQuest statuscode: {}", status);
                    logger.error("MapQuest messages {}", jsonNode.get("info").get("messages"));
                    return geocodedAddresses;
                }

                /** Fetch the results and translate them to GeocodedAddresses */
                JsonNode jsonResults = jsonNode.get("results");
                int numResults = jsonResults.size();

                /** Iterate over each result */
                for (int j = 0; j < numResults; j++) {
                    try {
                        JsonNode location = jsonResults.get(j).get("locations").get(0);
                        geocodedAddresses.add(getGeocodedAddressFromLocationNode(location));
                    }
                    catch (Exception ex) {
                        logger.warn("Error retrieving GeocodedAddress from MapQuest response {}", json, ex);
                        geocodedAddresses.add(new GeocodedAddress());
                    }
                }
            }
            return geocodedAddresses;
        }
        catch (MalformedURLException ex) {
            logger.error("Malformed MapQuest url!", ex);
        }
        catch (IOException ex) {
            logger.error("Error opening API resource! {} Response: {}", ex, json);
        }
        catch (NullPointerException ex) {
            logger.error("MapQuest response was not formatted correctly. Response: {}", json, ex);
        }
        catch (Exception ex){
            logger.error("{}", String.valueOf(ex));
        }
        return geocodedAddresses;
    }

    /**
     * Create an Address object by parsing the locations element of the json response.
     * @param location  A location node within the locations array that MapQuest returns.
     * @return          Address
     */
    private static Address getAddressFromLocationNode(JsonNode location) {
        var address = new Address();
        String addr1 = location.hasNonNull("street") ? location.get("street").asText() : "";
        addr1 = addr1.replaceFirst("^\\[.*]", "");
        address.setAddr1(addr1);
        address.setPostalCity(location.hasNonNull("adminArea5") ? location.get("adminArea5").asText() : "");
        address.setZip9(location.hasNonNull("postalCode") ? location.get("postalCode").asText() : "");
        return address;
    }

    /**
     * Create a Geocode object by parsing the locations element of the json response.
     * @param location  A location node within the locations array that MapQuest returns.
     * @return          Geocode
     */
    private static Geocode getGeocodeFromLocationNode(JsonNode location) {
        JsonNode latLon = location.get("latLng");
        Point point = new Point(latLon.get("lat").asDouble(), latLon.get("lng").asDouble());
        String qualityCode = location.get("geocodeQuality").asText();
        GeocodeQuality geocodeQuality = qualityMap.getOrDefault(qualityCode, GeocodeQuality.UNKNOWN);
        return new Geocode(point, geocodeQuality, HttpMapQuestDao.class.getSimpleName());
    }

    /**
     * Helper method to return GeocodedAddress from location element of the json response.
     * @param location A location node within the locations array that MapQuest returns.
     * @return         GeocodedAddress
     */
    private static GeocodedAddress getGeocodedAddressFromLocationNode(JsonNode location) {
        Address addr = getAddressFromLocationNode(location);
        Geocode geocode = getGeocodeFromLocationNode(location);
        return new GeocodedAddress(addr, geocode);
    }
}