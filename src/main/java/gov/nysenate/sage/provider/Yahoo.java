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
import gov.nysenate.sage.service.geo.GeocodeService;
import gov.nysenate.sage.service.geo.ParallelGeocodeService;
import gov.nysenate.sage.util.Config;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.log4j.Logger;
import org.json.JSONException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class Yahoo implements GeocodeService, Observer
{
    private static final String DEFAULT_BASE_URL = "http://query.yahooapis.com/v1/public/yql";
    private final Logger logger = Logger.getLogger(Yahoo.class);
    private Config config;
    private String baseUrl;
    private ObjectMapper objectMapper;

    public Yahoo()
    {
        this.config = ApplicationFactory.getConfig();
        this.objectMapper = new ObjectMapper();
        configure();
        logger.info("Initialized Yahoo Adapter");
    }

    private void configure()
    {
        baseUrl = config.getValue("yahoo.url");
        if (baseUrl.isEmpty()) {
            baseUrl = DEFAULT_BASE_URL;
        }
        config.notifyOnChange(this);
    }

    public void update(Observable o, Object arg)
    {
        configure();
    }

    @Override
    public GeocodeResult geocode(Address address)
    {
        if (address == null) {
            return null;
        }

        String yql = "select * from geo.placefinder where text=\"" + address.toString() + "\"";
        return getGeocodeResultFromYahoo(yql);
    }

    /**
     * Yahoo doesn't implement batch geocoding so we use the single address
     * geocoding method in parallel for performance improvements on our end.
    */
    @Override
    public ArrayList<GeocodeResult> geocode(ArrayList<Address> addresses)
    {
        return ParallelGeocodeService.geocode(this, addresses);
    }

    @Override
    public GeocodeResult reverseGeocode(Point point)
    {
        if (point == null) {
            return null;
        }

        String yql = "select * from geo.placefinder where text=\"" + point.getLat() + "," + point.getLon() +
                     "\" and gflags=\"R\"";

        return getGeocodeResultFromYahoo(yql);
    }

    private GeocodeResult getGeocodeResultFromYahoo(String yql)
    {
        GeocodeResult geocodeResult = new GeocodeResult();
        GeocodedAddress geocodedAddress = null;

        try {
            String encodedYql = URLEncoder.encode(yql, "UTF-8");
            String url = baseUrl +"?format=json&q="+encodedYql;
            logger.debug(url);

            Content content = Request.Get(url).execute().returnContent();
            String json = content.asString();

            JsonNode jsonNode = objectMapper.readTree(json).get("query");
            int resultCount = jsonNode.get("count").asInt();
            JsonNode resultsNode = jsonNode.get("results");

            /** Retrieve the first result */
            if (resultCount == 1) {
                geocodedAddress = getGeocodedAddressFromJson(resultsNode.get("Result"));
            }
            else if (resultCount > 1) {
                geocodedAddress = getGeocodedAddressFromJson(resultsNode.get("Result").get(0));
            }

            if (geocodedAddress != null) {
                geocodeResult.setGeocodedAddress(geocodedAddress);
            }
            else {
                geocodeResult.addMessage("Failed to retrieve a geocoded address from the response");
            }
            return geocodeResult;
        }
        catch (UnsupportedEncodingException ex) {
            String msg = "UTF-8 encoding not supported!?";
            logger.error(msg, ex);
        }
        catch (MalformedURLException ex) {
            String msg = "Malformed URL '" + geocodeResult.getSource() + "', check API key and address values.";
            logger.error(msg, ex);
        }
        catch (IOException ex) {
            String msg = "Error opening API resource '" + geocodeResult.getSource() + "'";
            logger.error(msg, ex);
        }
        catch (NullPointerException ex){
            logger.error("Error while parsing JSON result!", ex);
        }
        catch (Exception ex){
            logger.error(ex.getMessage());
        }
        return null;
    }

    /**
     * ...
     * @param resultNode
     * @return
     * @throws JSONException
     */
    private GeocodedAddress getGeocodedAddressFromJson(JsonNode resultNode)
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
     * @param quality   Int provided by Yahoo response
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