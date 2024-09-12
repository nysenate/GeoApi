package gov.nysenate.sage.dao.provider.google;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.util.UrlRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Repository
public class HttpGoogleDao implements GoogleDao {
    private static final Logger logger = LoggerFactory.getLogger(HttpGoogleDao.class);
    private static final String DEFAULT_BASE_URL = "https://maps.googleapis.com/maps/api/geocode/json";
    private static final String GEOCODE_QUERY = "?address=%s&key=%s";
    private static final String REV_GEOCODE_QUERY = "?latlng=%s&key=%s";
    private static final String ZIP_CODE_QUERY = "?components=postal_code:%s|country:US&key=%s";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final String baseUrl;
    private final String apiKey;

    @Autowired
    public HttpGoogleDao(Environment env) {
        String tempUrl = env.getGoogleGeocoderUrl();
        this.baseUrl = tempUrl == null || tempUrl.isEmpty() ? DEFAULT_BASE_URL : tempUrl;
        this.apiKey = env.getGoogleGeocoderKey();
    }

    /**
     * This method performs geocoding.
     * Retrieves a GeocodedAddress given an Address using Google.
     *
     * @param address   Address to geocode
     * @return          GeocodedAddress containing best matched Geocode.
     *                  null if there was a fatal error
     */
    public GeocodedAddress getGeocodedAddress(Address address) {
        GeocodedAddress geocodedAddress = null;

        try {
            String formattedQuery;
            if (address.getAddr1().isEmpty() && address.getZip5() != null) {
                logger.info("Input address is a zip code");
                formattedQuery = String.format(ZIP_CODE_QUERY, address.getZip5(), apiKey);
            }
            else {
                formattedQuery = String.format(GEOCODE_QUERY, URLEncoder.encode(address.toString(), StandardCharsets.UTF_8), apiKey);
            }
            String url = baseUrl + formattedQuery;
            geocodedAddress = getGeocodedAddress(url);
            if (geocodedAddress == null) {
                geocodedAddress = new GeocodedAddress(address);
            } else {
                geocodedAddress.setAddress(address);
            }
        } catch (NullPointerException ex) {
            logger.error("Null pointer while performing google geocode!", ex);
        }
        return geocodedAddress;
    }

    /**
     * This method performs reverse geocoding.
     * Retrieves a GeocodedAddress given a Point using Google.
     *
     * @param point Point to reverse geocode.
     * @return      GeocodedAddress containing best matched Address.
     */
    public GeocodedAddress getGeocodedAddress(Point point) {
        GeocodedAddress geocodedAddress = null;
        try {
            String formattedQuery = String.format(REV_GEOCODE_QUERY, point.toString(), apiKey);
            String url = baseUrl + formattedQuery;
            geocodedAddress = getGeocodedAddress(url); // Response is identical to address->geocode response.
        }
        catch (NullPointerException ex) {
            logger.error("Null pointer while performing google geocode!", ex);
        }
        return geocodedAddress;
    }

    /**
     * Sends out a request to google at the given url and returns a GeocodedAddress if successful.
     * @param url String
     * @return GeocodedAddress, or null if no match
     */
    private static GeocodedAddress getGeocodedAddress(String url) {
        GeocodedAddress geocodedAddress = null;

        try {
            String response = UrlRequest.getResponseFromUrl(url);
            if (response == null) {
                return null;
            }
            JsonNode node = objectMapper.readTree(response);
            if (node.has("status") && node.get("status").asText().equals("OK")) {
                JsonNode results = node.get("results");
                if (!results.isArray() || results.isEmpty()) return null;
                JsonNode result = results.get(0);
                String streetNumber = "", street = "", city = "", state = "", zip5 = "", zip4 = "";
                JsonNode addressComponents = result.get("address_components");
                for (JsonNode component : addressComponents) {
                    for (JsonNode type : component.get("types")) {
                        String typeText = type.asText();
                        switch (typeText) {
                            case "street_number":
                                streetNumber = component.get("long_name").asText();
                                break;
                            case "route":
                                street = component.get("long_name").asText();
                                break;
                            case "locality":
                                city = component.get("long_name").asText();
                                break;
                            case "administrative_area_level_3":
                                if (city.isEmpty()) {
                                    city = component.get("short_name").asText();
                                }
                                break;
                            case "administrative_area_level_1":
                                state = component.get("short_name").asText();
                                break;
                            case "postal_code":
                                zip5 = component.get("short_name").asText();
                                break;
                            case "postal_code_suffix":
                                zip4 = component.get("short_name").asText();
                                break;
                        }
                    }
                }
                String addr1 = streetNumber + " " + street;
                Address address = new Address(addr1, "", city, state, zip5, zip4);
                JsonNode location = result.get("geometry").get("location");
                double lat = location.get("lat").asDouble(0.0);
                double lon = location.get("lng").asDouble(0.0);
                String geocodeType = result.get("types").get(0).asText();
                Geocode geocode = new Geocode(
                        new Point(lat, lon), resolveGeocodeQuality(geocodeType), HttpGoogleDao.class.getSimpleName());
                geocodedAddress = new GeocodedAddress(address, geocode);
            }
            else if (node.has("status") && node.get("status").asText().equals("OVER_QUERY_LIMIT")) {
                logger.warn("Google geocoder is reporting that we have exceeded the query limit!");
            }
        }
        catch (IOException ex) {
            logger.error("Failed to retrieve data from google api!", ex);
        }
        catch (NullPointerException ex) {
            logger.error("NullPointerException while parsing google geocoder response!", ex);
        }
        return geocodedAddress;
    }

    private static GeocodeQuality resolveGeocodeQuality(String type) {
        return switch (type) {
            // House matches
            case "premise", "subpremise", "point_of_interest", "park", "natural_feature", "airport", "street_address" ->
                    GeocodeQuality.HOUSE;
            case "route", "intersection" -> GeocodeQuality.STREET;
            case "neighborhood", "sublocality", "locality" -> GeocodeQuality.CITY;
            case "administrative_area_level_2" -> GeocodeQuality.COUNTY;
            case "administrative_area_level_1" -> GeocodeQuality.STATE;
            case "postal_code" -> GeocodeQuality.ZIP;
            case "postal_code_suffix" -> GeocodeQuality.ZIP_EXT;
            default -> GeocodeQuality.UNKNOWN;
        };
    }
}
