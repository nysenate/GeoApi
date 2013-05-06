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
import gov.nysenate.sage.service.geo.ParallelGeocodeService;
import gov.nysenate.sage.util.Config;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * Yahoo Boss - Paid geo-coding service
 */
public class YahooBoss implements GeocodeService, Observer
{
    private static final String DEFAULT_BASE_URL = "http://yboss.yahooapis.com/geo/placefinder";
    private final Logger logger = Logger.getLogger(YahooBoss.class);
    private ObjectMapper objectMapper = new ObjectMapper();
    private Config config;
    private String baseUrl;
    private String consumerKey;
    private String consumerSecret;

    public YahooBoss() throws Exception
    {
        config = ApplicationFactory.getConfig();
        configure();
        logger.info("Initialized Yahoo Adapter");
    }

    public void update(Observable o, Object arg)
    {
        configure();
    }

    /**
     *  Yahoo doesn't implement batch geocoding so we use the single address geocoding
     *  method in parallel for performance improvements on our end.
     */
    public ArrayList<GeocodeResult> geocode(ArrayList<Address> addresses)
    {
        return ParallelGeocodeService.geocode(this, addresses);
    }


    public GeocodeResult geocode(Address address)
    {
        GeocodeResult geocodeResult = new GeocodeResult(this.getClass());

        if (address == null) {
            return geocodeResult;
        }

        try {
            /** Parse the API response */
            String urlText = baseUrl +"?flags=J&location="+URLEncoder.encode(address.toString(), "UTF-8").replace("+", "%20");
            logger.info(urlText);

            URL u = new URL(urlText);
            HttpURLConnection uc = (HttpURLConnection)u.openConnection();
            OAuthConsumer consumer = new DefaultOAuthConsumer(consumerKey, consumerSecret);
            consumer.sign(uc);
            int httpRespCode = uc.getResponseCode();

            if (httpRespCode != HttpURLConnection.HTTP_OK) {
                geocodeResult.setStatusCode(ResultStatus.RESPONSE_ERROR);
                geocodeResult.addMessage(IOUtils.toString(uc.getErrorStream()));
                return geocodeResult;
            }

            String body = IOUtils.toString(uc.getInputStream());

            JsonNode bossResponse = objectMapper.readTree(body).get("bossresponse");
            String status = bossResponse.get("responsecode").asText();
            if (!status.equals("200")) {
                geocodeResult.setStatusCode(ResultStatus.RESPONSE_ERROR);
                geocodeResult.addMessage(bossResponse.get("reason").asText());
                return geocodeResult;
            }

            JsonNode resultSet = bossResponse.get("placefinder").get("results");
            for (int i = 0; i < resultSet.size(); i++) {
                GeocodedAddress geocodedAddress = getGeocodedAddressFromResultNode(resultSet.get(i));
                geocodeResult.setGeocodedAddress(geocodedAddress);
                geocodeResult.setStatusCode(ResultStatus.SUCCESS);
            }

            return geocodeResult;
        }
        catch (UnsupportedEncodingException e) {
            String msg = "UTF-8 encoding not supported!?";
            logger.error(msg);
        }
        catch (MalformedURLException e) {
            String msg = "Malformed URL '"+geocodeResult.getSource()+"', check API key and address values.";
            logger.error(msg, e);
        }
        catch (IOException e) {
            String msg = "Error opening API resource '"+geocodeResult.getSource()+"'";
            logger.error(msg, e);
            geocodeResult.setStatusCode(ResultStatus.RESPONSE_ERROR);
            geocodeResult.addMessage(e.getMessage());
            return geocodeResult;
        }
        catch (OAuthMessageSignerException e) {
            String msg = "OAuthMessageSignerException";
            logger.error(msg, e);
        }
        catch (OAuthExpectationFailedException e) {
            String msg = "OAuthExpectationFailedException";
            logger.error(msg, e);
        }
        catch (OAuthCommunicationException e) {
            String msg = "OAuthCommunicationException";
            logger.error(msg, e);
        }
        return null;
    }

    @Override
    public GeocodeResult reverseGeocode(Point point)
    {
        return null;
    }

    @Override
    public ArrayList<GeocodeResult> reverseGeocode(ArrayList<Point> points)
    {
        return null;
    }

    private void configure()
    {
        config.notifyOnChange(this);
        baseUrl = config.getValue("yahoo.boss.url");
        consumerKey = config.getValue("yahoo.boss.consumer_key");
        consumerSecret = config.getValue("yahoo.boss.consumer_secret");

        if (baseUrl.isEmpty()) {
            baseUrl = DEFAULT_BASE_URL;
        }
    }


    private GeocodedAddress getGeocodedAddressFromResultNode(JsonNode jsonRes)
    {
        String street = (jsonRes.get("line1") != null) ? jsonRes.get("line1").asText() : null;
        String city = (jsonRes.get("city") != null) ? jsonRes.get("city").asText() : null;
        String state = (jsonRes.get("statecode") != null) ? jsonRes.get("statecode").asText() : null;
        String postal = (jsonRes.get("postal") != null) ? jsonRes.get("postal").asText() : null;
        int quality = (jsonRes.get("quality") != null) ? jsonRes.get("quality").asInt() : 0;
        double lat = (jsonRes.get("offsetlat") != null) ? jsonRes.get("offsetlat").asDouble() : 0.0;
        double lng = (jsonRes.get("offsetlon") != null) ? jsonRes.get("offsetlon").asDouble() : 0.0;

        Address resultAddress = new Address(street, city, state, postal);
        Geocode geocode = new Geocode(new Point(lat,lng), GeocodeQuality.UNKNOWN);

        return new GeocodedAddress(resultAddress, geocode);
    }
}
