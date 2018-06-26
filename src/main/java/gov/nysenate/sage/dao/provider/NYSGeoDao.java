package gov.nysenate.sage.dao.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.address.StreetAddress;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.util.Config;
import gov.nysenate.sage.util.StreetAddressParser;
import gov.nysenate.sage.util.UrlRequest;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class NYSGeoDao {

    private static final Logger logger = Logger.getLogger(NYSGeoDao.class);

    private static final Config config = ApplicationFactory.getConfig();
    private static final String DEFAULT_BASE_URL = config.getValue("nys.geocoder.url");
    private static final String GEOCODE_QUERY = config.getValue("nys.geocoder.ext")
            + "?street=%s&city=%s&state=NY&zip=%s";
    private static final String REV_GEOCODE_QUERY = config.getValue("nys.revgeocoder.ext")
            + "?location=%s&returnIntersection=false";
    private static final String COMMON_PARAMS = "&outSR=4326&f=pjson";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public NYSGeoDao() {}

    /**
     * This method performs geocoding.
     * Retrieves a GeocodedAddress given an Address using the NYS geocoder.
     *
     * @param address   Address to geocode
     * @return          GeocodedAddress containing best matched Geocode.
     *                  null if there was a fatal error
     */
    public GeocodedAddress getGeocodedAddress(Address address)
    {
        StreetAddress streetAddress = StreetAddressParser.parseAddress(address);
        address = new Address(streetAddress);
        GeocodedAddress geocodedAddress = null;

        try {
            String formattedQuery = String.format(GEOCODE_QUERY, URLEncoder.encode(address.toString(), "UTF-8"));
            String url = DEFAULT_BASE_URL + formattedQuery + COMMON_PARAMS;
            geocodedAddress = getGeocodedAddress(url, false);
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
     * This method performs geocoding.
     * Retrieves a GeocodedAddress given an Address using the NYS geocoder.
     *
     * @param point   Point to geocode
     * @return          GeocodedAddress containing best matched Geocode.
     *                  null if there was a fatal error
     */
    public GeocodedAddress getGeocodedAddress(Point point)
    {
        GeocodedAddress geocodedAddress = null;
        try {
            String formattedQuery = String.format(REV_GEOCODE_QUERY, point.toString());
            String url = DEFAULT_BASE_URL + formattedQuery + COMMON_PARAMS;
            geocodedAddress = getGeocodedAddress(url, true); // Response is identical to address->geocode response.
        }
        catch (NullPointerException ex) {
            logger.error("Null pointer while performing google geocode!", ex);
        }
        return geocodedAddress;
    }

    /**
     * Sends out a request to the NYS geocoder at the given url and returns a GeocodedAddress if successful.
     * @param url String
     * @return GeocodedAddress, or null if no match
     */
    private GeocodedAddress getGeocodedAddress(String url, boolean isRevGeocode) {
        GeocodedAddress geocodedAddress = null;

        try {
            String response = UrlRequest.getResponseFromUrl(url);
            if (response != null) {
                JsonNode node = objectMapper.readTree(response);
                double lat = 0.0;
                double lon = 0.0;
                int score = -1;
                Address address = null;

                if (isRevGeocode && node.has("address") && node.get("address") != null) {
                    JsonNode addressNode = node.get("address");
                    address = new Address(addressNode.get("Street").toString(),
                            addressNode.get("City").toString(), addressNode.get("State").toString(),
                            addressNode.get("ZIP").toString());
                    JsonNode location = node.get("location");
                    lat = location.get("x").asDouble();
                    lon = location.get("y").asDouble();
                }
                else if (node.has("candidates") && node.get("candidates") != null) {
                    JsonNode candidate = node.get("candidates").get(0);
                    address = new Address(StreetAddressParser.parseAddress(candidate.get("address").toString()));

                    if (candidate.has("score") && candidate.get("score") != null) {
                        score = candidate.get("score").asInt();
                    }
                    JsonNode location = candidate.get("location");
                    lat = location.get("x").asDouble();
                    lon = location.get("y").asDouble();
                }

                Geocode geocode = new Geocode( new Point(lat, lon),
                        resolveGeocodeQuality(score, isRevGeocode), NYSGeoDao.class.getSimpleName());
                geocodedAddress = new GeocodedAddress(address, geocode);
            }
        }
        catch (IOException ex) {
            logger.error("Failed to retrieve data from NYS Geo api!", ex);
        }
        catch (NullPointerException ex) {
            logger.error("NullPointerException while parsing NYS Geocoder response!", ex);
        }
        return geocodedAddress;
    }

    /**
     * Determines the geocode quality of the geocode or the revgeocode
     * @param quality quality rating from json response
     * @param isRevGeocode - whether the request was a revgeocode or not
     * @return geoQuality - the closest matching quality reference
     */
    private GeocodeQuality resolveGeocodeQuality(int quality, boolean isRevGeocode)
    {
        GeocodeQuality geoQuality = null;
        if (isRevGeocode) {
            geoQuality = GeocodeQuality.UNKNOWN;
        }
        else if (quality == 100) {
            geoQuality = GeocodeQuality.POINT;
        }
        else if (quality >= 90) {
            geoQuality = GeocodeQuality.HOUSE;
        }
        else if (quality == 0){
            geoQuality = GeocodeQuality.NOMATCH;
        }
        else if (quality < 90) {
            geoQuality = GeocodeQuality.UNKNOWN;
        }
        return geoQuality;
    }
}
