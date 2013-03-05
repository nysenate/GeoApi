package gov.nysenate.sage.dao.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.util.FormatUtil;
import gov.nysenate.sage.util.UrlRequest;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import static gov.nysenate.sage.model.geo.GeocodeQuality.*;
import static gov.nysenate.sage.model.geo.GeocodeQuality.ZIP_EXT;

public class MapQuestDao
{
    private static final String DEFAULT_GEO_URL = "http://www.mapquestapi.com/geocoding/v1/batch";
    private static final String DEFAULT_REV_URL = "http://www.mapquestapi.com/geocoding/v1/reverse";
    private static final String DEFAULT_FORMAT = "&outFormat=json&thumbMaps=false&maxResults=1";
    private static final int BATCH_SIZE = 95;

    private Logger logger = Logger.getLogger(MapQuestDao.class);
    private ObjectMapper objectMapper;
    private String geoUrl;
    private String revGeoUrl;
    private String key;

    private static HashMap<String, GeocodeQuality> qualityMap;
    static {
        /**
         * Map the quality codes to GeocodeQuality values.
         *
         * Point Values Based on Yahoo's quality point scheme
         * http://developer.yahoo.com/geo/placefinder/guide/responses.html#address-quality
         *
         * and the MapQuest geocode quality codes
         * http://www.mapquestapi.com/geocoding/geocodequality.html
         */
        qualityMap = new HashMap<>();
        qualityMap.put("POINT", POINT);
        qualityMap.put("ADDRESS", HOUSE);
        qualityMap.put("INTERSECTION", STREET);
        qualityMap.put("STREET", STREET);
        qualityMap.put("COUNTY", COUNTY);
        qualityMap.put("CITY", CITY);
        qualityMap.put("STATE", STATE);
        qualityMap.put("COUNTRY", STATE);
        qualityMap.put("ZIP", ZIP);
        qualityMap.put("ZIP_EXTENDED", ZIP_EXT);
    }

    public MapQuestDao() {
        this.objectMapper = new ObjectMapper();
    }

    public String getGeoUrl() {
        return (this.geoUrl != null && !this.geoUrl.isEmpty()) ? this.geoUrl : DEFAULT_GEO_URL;
    }

    public void setGeoUrl(String geoUrl) {
        this.geoUrl = geoUrl;
    }

    public String getRevGeoUrl() {
        return (this.revGeoUrl != null && !this.revGeoUrl.isEmpty()) ? this.revGeoUrl : DEFAULT_REV_URL;
    }

    public void setRevGeoUrl(String revGeoUrl) {
        this.revGeoUrl = revGeoUrl;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    /**
     * This method performs batch geocoding.
     * Retrieves a GeocodedAddress given an Address using Yahoo.
     *
     * @param addresses Addresses to geocode
     * @return          ArrayList of GeocodedAddress containing best matched Geocodes.
     */
    public ArrayList<GeocodedAddress> getGeocodedAddresses(ArrayList<Address> addresses)
    {
        ArrayList<GeocodedAddress> geocodedAddresses = new ArrayList<>();
        String url = getGeoUrl() + "?key=" + getKey() + DEFAULT_FORMAT;
        for (int i = 1; i <= addresses.size(); i++)
        {
            try {
                Address address = addresses.get(i-1);
                if (address == null) {
                    url += String.format("&location=%s", "null");
                }
                else {
                    url += String.format("&location=%s", URLEncoder.encode(address.toString(), "UTF-8"));
                }
                /** Stop here unless we've filled this batch request */
                if (i % BATCH_SIZE != 0 && i != addresses.size()) continue;

                /** Get GeocodedAddress objects from the request url and append them.
                  * If the batch result failed then return null, we don't want incomplete data */
                ArrayList<GeocodedAddress> batchResults = getGeocodedAddresses(url);
                if (batchResults != null){
                    geocodedAddresses.addAll(batchResults);
                }
                else {
                    return null;
                }
            }
            catch (UnsupportedEncodingException ex){
                logger.fatal(ex);
            }
        }
        return geocodedAddresses;
    }

    public GeocodedAddress getGeocodedAddress(Point point)
    {
        String url = getRevGeoUrl() + "?key=" + getKey() + DEFAULT_FORMAT + String.format("&lat=%f&lng=%f", point.getLat(), point.getLon());
        ArrayList<GeocodedAddress> revGeocodedAddresses = getGeocodedAddresses(url);

        if (revGeocodedAddresses != null && !revGeocodedAddresses.isEmpty()){
            return revGeocodedAddresses.get(0);
        }
        return null;
    }

    /**
     * Abstraction method that makes a request to the given MapQuest url, parses
     * the response, and returns a list of GeocodedAddresses.
     * @param url   The query url
     *              e.g. http://www.mapquestapi.com/geocoding/v1/address?..blah
     * @return      GeocodedAddress, or null on parse failure
     */
    private ArrayList<GeocodedAddress> getGeocodedAddresses(String url)
    {
        try {
            ArrayList<GeocodedAddress> geocodedAddresses = new ArrayList<>();

            /** Format and send request */
            String json = UrlRequest.getResponseFromUrl(url);
            JsonNode jsonNode = objectMapper.readTree(json);

            /** Log any response errors and return null if so */
            String status = jsonNode.get("info").get("statuscode").asText();
            if (status != "0"){
                logger.debug("MapQuest statuscode: " + status);
                logger.error("MapQuest messages " + jsonNode.get("info").get("messages"));
                return null;
            }

            /** Fetch the results and translate them to GeocodedAddresses */
            JsonNode jsonResults = jsonNode.get("results");
            int numResults = jsonResults.size();

            /** Iterate over each result */
            for (int j = 0; j < numResults; j++) {

                /** Get location node from locations array */
                JsonNode location = jsonResults.get(j).get("locations").get(0);

                /** Build the Address from the location node */
                Address addr = getAddressFromLocationNode(location);

                /** Build the Geocode */
                Geocode geocode = getGeocodeFromLocationNode(location);

                /** Add the GeocodedAddress to the batch list */
                geocodedAddresses.add(new GeocodedAddress(addr, geocode));
            }

            return geocodedAddresses;
        }
        catch (MalformedURLException ex){
            logger.error("Malformed MapQuest url!", ex);
        }
        catch (UnsupportedEncodingException ex){
            logger.error("UTF-8 Unsupported?!", ex);
        }
        catch (JsonProcessingException ex){
            logger.error("MapQuest JSON Parse error!", ex);
        }
        catch (Exception ex){
            logger.error(ex);
        }
        return null;
    }

    /**
     * Create an Address object by parsing the locations element of the json response.
     * @param location  A location node within the locations array that MapQuest returns.
     * @return          Address
     */
    private Address getAddressFromLocationNode(JsonNode location)
    {
        Address address = new Address();
        address.setAddr1(location.get("street").asText());
        address.setCity(location.get("adminArea5").asText());
        address.setState(location.get("adminArea3").asText());
        address.setPostal(location.get("postalCode").asText());
        return address;
    }

    /**
     * Create a Geocode object by parsing the locations element of the json response.
     * @param location  A location node within the locations array that MapQuest returns.
     * @return          Geocode
     */
    private Geocode getGeocodeFromLocationNode(JsonNode location)
    {
        JsonNode latLon = location.get("latLng");
        Point point = new Point(latLon.get("lat").asDouble(), latLon.get("lng").asDouble());
        String qualityCode = location.get("geocodeQuality").asText();
        GeocodeQuality geocodeQuality = (qualityMap.containsKey(qualityCode)) ? qualityMap.get(qualityCode) : GeocodeQuality.UNKNOWN;
        return new Geocode(point, geocodeQuality, this.getClass().getSimpleName());
    }
}