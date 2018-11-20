package gov.nysenate.sage.dao.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.util.Config;
import gov.nysenate.sage.util.UrlRequest;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 *  Provides a data abstraction layer for performing Yahoo Placefinder requests.
 *  This is the free non-commercial Yahoo service that utilizes YQL queries.
 *
 *  Documentation available here:
 *  http://developer.yahoo.com/boss/geo/docs/free_YQL.html#table_pf
 */
@Repository
public class YahooDao
{
    private static final String DEFAULT_BASE_URL = "http://query.yahooapis.com/v1/public/yql";
    private static final String SET_QUERY_AS = "?format=json&crossProduct=optimized&q=";
    private static final String GEOCODE_QUERY = "select line1, city, statecode, postal, quality, latitude, longitude " +
                                                "from geo.placefinder where text=\"%s\"";
    private static final String BATCH_GEOCODE_QUERY = "select line1, city, statecode, postal, quality, latitude, longitude " +
                                                "from geo.placefinder(0,1) where %s";
    private static final String REVERSE_GEO_QUERY = "select * from geo.placefinder where text=\"%f,%f\" and gflags=\"R\"";

    private static String CONSUMER_KEY;
    private static String CONSUMER_SECRET;
    private static int BATCH_SIZE = 100;

    private Logger logger = LoggerFactory.getLogger(YahooDao.class);
    private String baseUrl;
    private static ObjectMapper objectMapper = new ObjectMapper();

    private BaseDao baseDao;
    private Environment env;

    @Autowired
    public YahooDao(BaseDao baseDao, Environment env)
    {
        this.baseDao = baseDao;
        this.env = env;
        this.baseUrl = this.env.getYahooUrl();
        this.CONSUMER_KEY = this.env.getYahooConsumerKey();
        this.CONSUMER_SECRET = this.env.getYahooConsumerSecret();
        this.BATCH_SIZE = this.env.getYahooBatchSize();
    }

    public String getBaseUrl()
    {
        return (this.baseUrl != null && !this.baseUrl.isEmpty()) ? this.baseUrl : DEFAULT_BASE_URL;
    }

    /**
     * This method performs geocoding.
     * Retrieves a GeocodedAddress given an Address using Yahoo.
     *
     * @param address   Address to geocode
     * @return          GeocodedAddress containing best matched Geocode.
     *                  null if there was a fatal error
     */
    public GeocodedAddress getGeocodedAddress(Address address)
    {
        GeocodedAddress geocodedAddress = null;

        try {
            String formattedQuery = URLEncoder.encode(String.format(GEOCODE_QUERY, address.toString()), "UTF-8");
            String url = getBaseUrl() + SET_QUERY_AS + formattedQuery;
            geocodedAddress = getGeocodedAddress(url);
        }
        catch (UnsupportedEncodingException ex) {
            logger.error("UTF-8 encoding not supported!?", ex);
        }
        catch (NullPointerException ex) {
            logger.error("" + ex);
        }
        return geocodedAddress;
    }

    /**
     * Batch geocode a list of addresses.
     * @param addresses Addresses to geocode
     * @return          List<GeocodedAddress>
     */
    public List<GeocodedAddress> getGeocodedAddresses(List<Address> addresses)
    {
        /** Pre-populate the batch result with addresses */
        List<GeocodedAddress> geocodedAddresses = new ArrayList<>();
        for (Address address : addresses) {
            geocodedAddresses.add(new GeocodedAddress(address));
        }

        try {
            int batchOffset = 0;
            List<String> locations = new ArrayList<>();
            for (int i = 1; i <= addresses.size(); i++) {
                locations.add(String.format("text=\"%s\"", addresses.get(i - 1).toString()));

                /** Stop here unless we've filled this batch request */
                if (i % BATCH_SIZE != 0 && i != addresses.size()) continue;

                String whereClause = StringUtils.join(locations, " or ");
                String url = getBaseUrl() + SET_QUERY_AS + URLEncoder.encode(String.format(BATCH_GEOCODE_QUERY, whereClause), "UTF-8");
                List<GeocodedAddress> batchResults = getGeocodedAddresses(url);

                /** A successful response will have the same size */
                if (batchResults.size() == locations.size()) {
                    for (int j = 0; j < locations.size(); j++) {
                        if (batchResults.get(j) != null && batchResults.get(j).isValidGeocode()) {
                            geocodedAddresses.set(j + batchOffset, batchResults.get(j));
                        }
                    }
                }
                else {
                    logger.warn("Expected response size: " + locations.size() + ", received: " + batchResults.size());
                    logger.warn("Skipping failed Yahoo batch (" + batchOffset + " - " + (batchOffset + locations.size()) + ")");
                }
                batchOffset += locations.size();
                locations.clear();
            }
        }
        catch (UnsupportedEncodingException ex) {
            logger.error("UTF-8 encoding not supported!?", ex);
        }
        catch (NullPointerException ex) {
            logger.error("" + ex);
        }
        return geocodedAddresses;
    }

    /**
     * This method performs reverse geocoding.
     * Retrieves a GeocodedAddress given a Point using Yahoo.
     *
     * @param point Point to reverse geocode.
     * @return      GeocodedAddress containing best matched Address.
     */
    public GeocodedAddress getGeocodedAddress(Point point)
    {
        GeocodedAddress geocodedAddress = null;
        try {
            String formattedQuery = URLEncoder.encode(String.format(REVERSE_GEO_QUERY, point.getLat(), point.getLon()), "UTF-8");
            String url = getBaseUrl() + SET_QUERY_AS + formattedQuery;
            geocodedAddress = getGeocodedAddress(url);
        }
        catch(UnsupportedEncodingException ex){
            logger.error("UTF-8 encoding not supported!?", ex);
        }
        return geocodedAddress;
    }

    /**
     * Abstraction method that makes a request to the given Yahoo url, parses
     * the response, and returns a GeocodedAddress. If an error occurs, the
     * method will return null.
     * @param url   The query url
     *              e.g. http://query.yahooapis.com/v1/public/yql?format=json&q=blah..
     * @return      GeocodedAddress, or null on parse failure
     */
    private GeocodedAddress getGeocodedAddress(String url)
    {
        GeocodedAddress geocodedAddress = null;
        String json = "";
        try {
            json = UrlRequest.getResponseFromUrlUsingOauth(url, CONSUMER_KEY, CONSUMER_SECRET);
            if (json != null) {
                JsonNode rootNode = objectMapper.readTree(json).get("query");
                JsonNode resultsNode = rootNode.get("results");
                int resultCount = rootNode.get("count").asInt();

                if (resultCount == 1) {
                    geocodedAddress = getGeocodedAddressFromResultNode(resultsNode.get("Result"));
                }
                else if (resultCount > 1) {
                    geocodedAddress = getGeocodedAddressFromResultNode(resultsNode.get("Result").get(0));
                }
            }
            else {
                logger.warn("No response from Yahoo's geocode service!");
            }
        }
        catch (MalformedURLException ex) {
            logger.error("Malformed URL! ", ex);
        }
        catch (IOException ex) {
            logger.error("Error opening API resource! " + ex.toString() + " Response: " + json);
        }
        catch (NullPointerException ex) {
            logger.error("Yahoo response was not formatted correctly! Response: " + json, ex);
        }
        return geocodedAddress;
    }

    /** Retrieve the GeocodedAddress objects from the response of the given query url.
     *  If an exception occurs while retrieving the response an empty array is returned.
     *  If an exception occurs while parsing a GeocodedAddress, an empty GeocodedAddress is appended. */
    private List<GeocodedAddress> getGeocodedAddresses(String url)
    {
        List<GeocodedAddress> geocodedAddresses = new ArrayList<>();
        String json = "";
        try {
            json = UrlRequest.getResponseFromUrlUsingOauth(url, CONSUMER_KEY, CONSUMER_SECRET);
            if (json != null) {
                JsonNode rootNode = objectMapper.readTree(json).get("query");
                JsonNode resultsNode = rootNode.get("results");
                int resultCount = rootNode.get("count").asInt();

                if (resultCount == 1) {
                    try {
                        geocodedAddresses.add(getGeocodedAddressFromResultNode(resultsNode.get("Result")));
                    }
                    catch (NullPointerException ex) {
                        logger.warn("Error retrieving GeocodedAddress from Yahoo response " + json, ex);
                        geocodedAddresses.add(new GeocodedAddress());
                    }
                }
                else {
                    for (int i = 0; i < resultCount; i++) {
                        try {
                            geocodedAddresses.add(getGeocodedAddressFromResultNode(resultsNode.get("Result").get(i)));
                        }
                        catch (NullPointerException ex) {
                            logger.warn("Error retrieving GeocodedAddress from Yahoo response " + json, ex);
                            geocodedAddresses.add(new GeocodedAddress());
                        }
                    }
                }
            }
            else {
                logger.warn("No response from Yahoo's batch geocode service!");
            }
        }
        catch (MalformedURLException ex) {
            logger.error("Malformed URL! ", ex);
        }
        catch (IOException ex) {
            logger.error("Error opening API resource! " + ex.toString());
        }
        catch (NullPointerException ex) {
            logger.error("Yahoo response was not formatted correctly. Response: " + json, ex);
        }
        return geocodedAddresses;
    }

    /**
     * Parses and returns GeocodedAddress from result JSON node.
     *
     * The resultNode is a reference to the "Result" node depicted by the following
     * response structure that is returned from yahoo:
     *
     * {"query": { ... "results": [{ "Result": { ..result data.. }}]}
     *
     * @param resultNode    The "Result" node
     * @return              The GeocodedAddress created from the "Result" data
     */
    private GeocodedAddress getGeocodedAddressFromResultNode(JsonNode resultNode)
    {
        String street = resultNode.hasNonNull("line1") ? resultNode.get("line1").asText() : "";
        String city = resultNode.hasNonNull("city") ? resultNode.get("city").asText() : "";
        String state = resultNode.get("statecode").asText();
        String postal = resultNode.hasNonNull("postal") ? resultNode.get("postal").asText() : "";
        GeocodeQuality quality = resolveGeocodeQuality(resultNode.get("quality").asInt());
        double lat = resultNode.get("latitude").asDouble(0.0);
        double lng = resultNode.get("longitude").asDouble(0.0);

        Address resultAddress = new Address(street, city, state, postal);
        Geocode resultGeocode = new Geocode(new Point(lat, lng), quality, YahooDao.class.getSimpleName());
        GeocodedAddress geocodedAddress = new GeocodedAddress(resultAddress, resultGeocode);
        return geocodedAddress;
    }

    /**
     * Yahoo has a detailed list of quality codes. Here we can condense the codes into
     * our basic GeocodeQuality levels.
     * @param quality   Integer code provided by Yahoo response
     * @return          Corresponding GeocodeQuality
     */
    private GeocodeQuality resolveGeocodeQuality(int quality)
    {
        if ( quality == 99 || quality == 90 ) return GeocodeQuality.POINT;
        if (quality >= 80) return GeocodeQuality.HOUSE;
        if (quality >= 74) return GeocodeQuality.ZIP_EXT;
        if (quality >= 70) return GeocodeQuality.STREET;
        if (quality >= 59) return GeocodeQuality.ZIP;
        if (quality >= 39) return GeocodeQuality.CITY;
        if (quality >= 29) return GeocodeQuality.COUNTY;
        if (quality >= 19) return GeocodeQuality.STATE;
        return GeocodeQuality.NOMATCH;
    }
}