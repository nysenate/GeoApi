package gov.nysenate.sage.dao.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.util.UrlRequest;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;

/**
 *  Provides a data abstraction layer for performing Yahoo Placefinder requests.
 *  This is the free non-commercial Yahoo service that utilizes YQL queries.
 *
 *  Documentation available here:
 *  http://developer.yahoo.com/boss/geo/docs/free_YQL.html#table_pf
 */
public class YahooDao
{
    private static final String DEFAULT_BASE_URL = "http://query.yahooapis.com/v1/public/yql";
    private static final String SET_QUERY_AS = "?format=json&q=";
    private static final String GEOCODE_QUERY = "select * from geo.placefinder where text=\"%s\"";
    private static final String REVERSE_GEO_QUERY = "select * from geo.placefinder where text=\"%f,%f\" and gflags=\"R\"";

    private Logger logger = Logger.getLogger(YahooDao.class);
    private String baseUrl;
    private ObjectMapper objectMapper;

    public YahooDao(){
        this.objectMapper = new ObjectMapper();
    }

    public String getBaseUrl() {
        return (this.baseUrl != null && !this.baseUrl.isEmpty()) ? this.baseUrl : DEFAULT_BASE_URL;
    }

    public void setBaseUrl(String baseUrl)
    {
        if (baseUrl != null && !baseUrl.isEmpty()){
            this.baseUrl = baseUrl;
        }
    }

    /**
     * This method performs geocoding.
     * Retrieves a GeocodedAddress given an Address using Yahoo.
     *
     * @param address   Address to geocode
     * @return          GeocodedAddress containing best matched Geocode.
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
        return geocodedAddress;
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
     * the response, and returns a GeocodedAddress.
     * @param url   The query url
     *              e.g. http://query.yahooapis.com/v1/public/yql?format=json&q=blah..
     * @return      GeocodedAddress, or null on parse failure
     */
    private GeocodedAddress getGeocodedAddress(String url)
    {
        GeocodedAddress geocodedAddress = null;
        try {
            String json = UrlRequest.getResponseFromUrl(url);
            JsonNode rootNode = objectMapper.readTree(json).get("query");
            JsonNode resultsNode = rootNode.get("results");
            int resultCount = rootNode.get("count").asInt();

            /** Retrieve the first result only. We can assume that results are ranked. */
            if (resultCount == 1) {
                geocodedAddress = getGeocodedAddressFromResultNode(resultsNode.get("Result"));
            }
            else if (resultCount > 1) {
                geocodedAddress = getGeocodedAddressFromResultNode(resultsNode.get("Result").get(0));
            }
        }
        catch (MalformedURLException ex) {
            logger.error("Malformed URL! ", ex);
        }
        catch (IOException ex) {
            logger.error("Error opening API resource!", ex);
        }
        return geocodedAddress;
    }

    /**
     * Parses and returns GeocodedAddress from result JSON node.
     *
     * The resultNode is a reference to the "Result" node depicted by the following
     * response structure that is returned from yahoo:
     *
     * {"query": { ... "results": { "Result": { ..result data.. }}}}
     *
     * @param resultNode    The "Result" node
     * @return              The GeocodedAddress created from the "Result" data
     */
    private GeocodedAddress getGeocodedAddressFromResultNode(JsonNode resultNode)
    {
        String street = resultNode.get("line1").asText();
        String city = resultNode.get("city").asText();
        String state = resultNode.get("statecode").asText();
        String postal = resultNode.get("postal").asText();
        GeocodeQuality quality = resolveGeocodeQuality(resultNode.get("quality").asInt());
        double lat = resultNode.get("latitude").asDouble(0.0);
        double lng = resultNode.get("longitude").asDouble(0.0);

        Address resultAddress = new Address(street, city, state, postal);
        Geocode resultGeocode = new Geocode(new Point(lat, lng), quality, this.getClass().getSimpleName());
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