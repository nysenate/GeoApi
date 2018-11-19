package gov.nysenate.sage.dao.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.util.Config;
import gov.nysenate.sage.util.UrlRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Observable;
import java.util.Observer;

@Repository
public class YahooBossDao implements Observer
{
    private static final Logger logger = LoggerFactory.getLogger(YahooBossDao.class);
    private final Config config;
    private static final String DEFAULT_BASE_URL = "http://yboss.yahooapis.com/geo/placefinder";
    private static String CONSUMER_KEY;
    private static String CONSUMER_SECRET;

    private String baseUrl;
    private ObjectMapper objectMapper;

    private BaseDao baseDao;

    @Autowired
    public YahooBossDao(BaseDao baseDao)
    {
        this.baseDao = baseDao;
        this.config = this.baseDao.getConfig();
        this.objectMapper = new ObjectMapper();
        this.update(null, null);
    }

    @Override
    public void update(Observable o, Object arg)
    {
        this.baseUrl = config.getValue("yahoo.boss.url", DEFAULT_BASE_URL);
        CONSUMER_KEY = config.getValue("yahoo.boss.consumer_key");
        CONSUMER_SECRET = config.getValue("yahoo.boss.consumer_secret");
    }

    public GeocodedAddress getGeocodedAddress(Address address)
    {
        try {
            String url = this.baseUrl +"?flags=J&location="+ URLEncoder.encode(address.toString(), "UTF-8").replace("+", "%2B");
            return getGeocodedAddress(url);
        }
        catch (UnsupportedEncodingException ex) {
            logger.error("" + ex);
        }
        return null;
    }

    private GeocodedAddress getGeocodedAddress(String url)
    {
        String json = "";
        try {
            /** Retrieve response from OAuth request */
            json = UrlRequest.getResponseFromUrlUsingOauth(url, CONSUMER_KEY, CONSUMER_SECRET);
            if (json != null) {
                JsonNode bossResponse = objectMapper.readTree(json).get("bossresponse");
                String status = bossResponse.get("responsecode").asText();

                /** Check for errors */
                if (!status.equals("200")) {
                    logger.error("Error with YahooBoss request: " + bossResponse.get("reason").asText());
                    return null;
                }

                /** Take the first result */
                JsonNode resultSet = bossResponse.get("placefinder").get("results");
                if (resultSet != null && resultSet.size() > 0 && resultSet.get(0) != null) {
                    return getGeocodedAddressFromResultNode(resultSet.get(0));
                }
            }
        }
        catch (IOException ex) {
            logger.error("Error opening API resource! " + ex.toString() + " Response: " + json);
        }
        return null;
    }

    private GeocodedAddress getGeocodedAddressFromResultNode(JsonNode jsonResult)
    {
        String street = (jsonResult.hasNonNull("line1")) ? jsonResult.get("line1").asText() : "";
        String city = (jsonResult.hasNonNull("city")) ? jsonResult.get("city").asText() : "";
        String state = (jsonResult.hasNonNull("statecode")) ? jsonResult.get("statecode").asText() : "";
        String postal = (jsonResult.hasNonNull("postal")) ? jsonResult.get("postal").asText() : "";
        int rawQuality = (jsonResult.hasNonNull("quality")) ? jsonResult.get("quality").asInt() : 0;
        double lat = (jsonResult.hasNonNull("offsetlat")) ? jsonResult.get("offsetlat").asDouble() : 0.0;
        double lng = (jsonResult.hasNonNull("offsetlon")) ? jsonResult.get("offsetlon").asDouble() : 0.0;

        Geocode geocode = new Geocode(new Point(lat,lng), resolveGeocodeQuality(rawQuality), this.getClass().getSimpleName());
        geocode.setRawQuality(rawQuality);

        return new GeocodedAddress(new Address(street, city, state, postal), geocode);
    }

    /**
     * Yahoo has a detailed list of quality codes. Here we can condense the codes into
     * our basic GeocodeQuality levels.
     *
     * Note: same as YahooDao's method but duplicated here in case it changes separately
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
