package gov.nysenate.sage.dao.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.util.Config;
import gov.nysenate.sage.util.UrlRequest;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Observable;
import java.util.Observer;

public class GoogleDao implements Observer
{
    private static final Logger logger = Logger.getLogger(GoogleDao.class);

    private static final Config config = ApplicationFactory.getConfig();
    private static final String DEFAULT_BASE_URL = "https://maps.googleapis.com/maps/api/geocode/json";
    private static final String GEOCODE_QUERY = "?address=%s&key=%s";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private String baseUrl;
    private String apiKey;

    public GoogleDao() {
        config.notifyOnChange(this);
        this.update(null, null);
    }

    @Override
    public void update(Observable o, Object arg)
    {
        this.baseUrl = config.getValue("google.geocoder.url", DEFAULT_BASE_URL);
        this.apiKey = config.getValue("google.geocoder.key", "");
    }

    public String getBaseUrl()
    {
        return (this.baseUrl != null && !this.baseUrl.isEmpty()) ? this.baseUrl : DEFAULT_BASE_URL;
    }

    /**
     * This method performs geocoding.
     * Retrieves a GeocodedAddress given an Address using Google.
     *
     * @param address   Address to geocode
     * @return          GeocodedAddress containing best matched Geocode.
     *                  null if there was a fatal error
     */
    public GeocodedAddress getGeocodedAddress(Address address)
    {
        GeocodedAddress geocodedAddress = null;

        try {
            String formattedQuery = String.format(GEOCODE_QUERY, URLEncoder.encode(address.toString(), "UTF-8"), apiKey);
            String url = getBaseUrl() + formattedQuery;
            geocodedAddress = getGeocodedAddress(url);
        }
        catch (UnsupportedEncodingException ex) {
            logger.error("UTF-8 encoding not supported!?", ex);
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
    private GeocodedAddress getGeocodedAddress(String url) {
        GeocodedAddress geocodedAddress = null;

        try {
            String response = UrlRequest.getResponseFromUrl(url);
            if (response != null) {
                JsonNode node = objectMapper.readTree(response);
                if (node.has("status") && node.get("status").asText().equals("OK")) {
                    JsonNode results = node.get("results");
                    if (!results.isArray() || results.size() < 1) return null;
                    JsonNode result = results.get(0);
                    String streetNumber = "", street = "", city = "", state = "", zip5 = "", zip4 = "";
                    JsonNode addressComponents = result.get("address_components");
                    for (JsonNode component : addressComponents) {
                        for (JsonNode type : component.get("types")) {
                            String typeText = type.asText();
                            switch (typeText) {
                                case "street_number":
                                    streetNumber = component.get("long_name").asText(); break;
                                case "route":
                                    street = component.get("long_name").asText(); break;
                                case "locality":
                                    city = component.get("long_name").asText(); break;
                                case "administrative_area_level_1":
                                    state = component.get("short_name").asText(); break;
                                case "postal_code":
                                    zip5 = component.get("short_name").asText(); break;
                                case "postal_code_suffix":
                                    zip4 = component.get("short_name").asText(); break;
                            }
                        }
                    }
                    String addr1 = streetNumber + " " + street;
                    Address address = new Address(addr1, "", city, state, zip5, zip4);
                    JsonNode location = result.get("geometry").get("location");
                    Double lat = location.get("lat").asDouble(0.0);
                    Double lon = location.get("lng").asDouble(0.0);
                    String geocodeType = result.get("types").get(0).asText();
                    Geocode geocode = new Geocode(
                            new Point(lat, lon), resolveGeocodeQuality(geocodeType), GoogleDao.class.getSimpleName());
                    geocodedAddress = new GeocodedAddress(address, geocode);
                }
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

    private GeocodeQuality resolveGeocodeQuality(String type)
    {
        switch (type) {
            case "premise":
            case "street_address": return GeocodeQuality.HOUSE;
            case "route": return GeocodeQuality.STREET;
            case "neighborhood":
            case "administrative_area_level_2":
            case "locality": return GeocodeQuality.CITY;
            case "administrative_area_level_1": return GeocodeQuality.STATE;
            case "postal_code": return GeocodeQuality.ZIP;
            case "postal_code_suffix": return GeocodeQuality.ZIP_EXT;
        }
        return GeocodeQuality.UNKNOWN;
    }
}
