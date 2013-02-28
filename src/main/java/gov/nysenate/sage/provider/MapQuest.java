package gov.nysenate.sage.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;
import static gov.nysenate.sage.model.geo.GeocodeQuality.*;

import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.AddressResult;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.service.address.AddressService;
import gov.nysenate.sage.service.geo.GeocodeService;
import gov.nysenate.sage.util.Config;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

import gov.nysenate.sage.util.UrlRequest;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.log4j.Logger;

/**
 * MapQuest Geocoding provider implementation*
 * Documentation: http://www.mapquestapi.com/geocoding/
 *
 * @author Graylin Kim, Ash Islam
 */
public class MapQuest implements AddressService, GeocodeService, Observer
{
    private static final String DEFAULT_BATCH_URL = "http://www.mapquestapi.com/geocoding/v1/batch";
    private static final String DEFAULT_REV_URL = "http://www.mapquestapi.com/geocoding/v1/reverse";
    private final Logger logger = Logger.getLogger(MapQuest.class);
    private Config config;
    ObjectMapper mapper;

    private final int BATCH_SIZE = 95;
    private final HashMap<String, GeocodeQuality> qualityMap;
    private String geoBaseUrl;
    private String revBaseUrl;

    public MapQuest()
    {
        this.config = ApplicationFactory.getConfig();
        this.mapper = new ObjectMapper();
        configure();

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

        logger.info("Initialized MapQuest Adapter");
    }

    public void update(Observable o, Object arg)
    {
        configure();
    }

    private void configure()
    {
        config.notifyOnChange(this);
        String baseUrl = config.getValue("mapquest.geo.url");
        String revBaseUrl = config.getValue("mapquest.rev.url");
        String apiKey = config.getValue("mapquest.key");

        if (baseUrl.isEmpty()) { baseUrl = DEFAULT_BATCH_URL; }
        if (revBaseUrl.isEmpty()) { revBaseUrl = DEFAULT_REV_URL; }

        /** Show only one result per location | Use JSON output | Don't bother with the map thumbnail images */
        this.geoBaseUrl = baseUrl+"?key="+apiKey+"&outFormat=json&thumbMaps=false&maxResults=1";
        this.revBaseUrl = revBaseUrl+"?key="+apiKey+"&outFormat=json&thumbMaps=false&maxResults=1";
    }

    /**
     * Geocodes a single address
     * @param address Address to geocode
     * @return        GeocodeResult or null on fatal error
     */
    @Override
    public GeocodeResult geocode(Address address)
    {
        ArrayList<GeocodeResult> results = geocode(new ArrayList<>(Arrays.asList(address)));
        if (results != null && results.size() > 0){
            return results.get(0);
        }
        return null;
    }

    /**
     * Geocodes multiple addresses in a batch. Mapquest provides a native batch
     * geocoding api so this is used for all requests.
     * @param addresses List of Addresses to geocode
     * @return          List of GeocodeResults
     */
    @Override
    public ArrayList<GeocodeResult> geocode(ArrayList<Address> addresses)
    {
        ArrayList<GeocodeResult> geocodeResults = new ArrayList<>();
        Address address;
        String url = geoBaseUrl;

        try {
            for (int a = 1; a <= addresses.size(); a++) {
                address = addresses.get(a-1);

                if (address == null) {
                    url += "&location=null";
                }
                else {
                    url += "&location=" + URLEncoder.encode(address.toString(), "UTF-8");
                }

                /** Stop here unless we've filled this batch request */
                if (a % BATCH_SIZE != 0 && a != addresses.size()) continue;

                String json = UrlRequest.getResponseFromUrl(url);
                logger.debug(json);

                JsonNode jsonNode = mapper.readTree(json);

                /** Get the status and kill the method if the status indicates a problem */
                String status = jsonNode.get("info").get("statuscode").asText();

                if (status != "0"){
                    logger.debug("MapQuest statuscode: " + status);
                    logger.error("MapQuest messages " + jsonNode.get("info").get("messages"));
                    return null;
                }

                /** Fetch the results and translate them to GeocodeResult objects */
                JsonNode jsonResults = jsonNode.get("results");
                int numResults = jsonResults.size();

                for (int i = 0; i < numResults; i++) {

                    GeocodeResult geocodeResult = new GeocodeResult(this.getClass());
                    GeocodedAddress geocodedAddress;
                    JsonNode location = jsonResults.get(i).get("locations").get(0);

                    /** Build the address */
                    Address addr = getAddressFromJson(location);

                    /** Build the Geocode */
                    Geocode geocode = new Geocode();
                    JsonNode latLon = location.get("latLng");

                    if (latLon != null) {
                        geocode.setLatlon(latLon.get("lat").asDouble(), latLon.get("lng").asDouble());
                        geocode.setMethod(this.getClass().getSimpleName());
                        String qualityCode = location.get("geocodeQuality").asText();
                        GeocodeQuality geocodeQuality = (qualityMap.containsKey(qualityCode)) ? qualityMap.get(qualityCode) : GeocodeQuality.UNKNOWN;
                        geocode.setQuality(geocodeQuality);

                        /** Check the quality, if it's below zip then it's not considered a match */
                        if (geocodeQuality.compareTo(GeocodeQuality.ZIP) < 0){
                            geocodeResult.setStatusCode(ResultStatus.NO_GEOCODE_RESULT);
                        }
                    }
                    else {
                        geocodeResult.setStatusCode(ResultStatus.NO_GEOCODE_RESULT);
                    }

                    /** Build the response */
                    geocodedAddress = new GeocodedAddress(addr,geocode);
                    geocodeResult.setGeocodedAddress(geocodedAddress);
                    geocodeResults.add(geocodeResult);
                }
            }
            return geocodeResults;
        }
        catch (UnsupportedEncodingException ex){
            logger.error("UTF-8 not supported? " + ex.getMessage());
        }
        catch (IOException ex){
            logger.error(ex.getMessage());
        }
        return null;
    }

    /**
     * Given a lat lng pair return the address that is closest to that point.
     * Mapquest does not have a bulk option for this operation.
     * @param point     Point to reverse geocode.
     * @return          GeocodeResult with matched GeocodeAddress or null if error.
     */
    @Override
    public GeocodeResult reverseGeocode(Point point)
    {
        if (point != null) {

            GeocodeResult geocodeResult = new GeocodeResult(this.getClass());

            /** Perform boxing */
            Double lat = point.getLat();
            Double lon = point.getLon();

            String url = revBaseUrl + "&lat=" + lat.toString() + "&lng=" + lon.toString();
            logger.debug(url);

            try {
                String json = UrlRequest.getResponseFromUrl(url);

                logger.debug(json);

                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonNode = mapper.readTree(json);

                /** Get the status and indicate if there's an error */
                String status = jsonNode.get("info").get("statuscode").asText();
                if (status != "0"){
                    logger.error("MapQuest Error: " + jsonNode.get("info").get("messages"));
                    geocodeResult.setStatusCode(ResultStatus.NO_REVERSE_GEOCODE_RESULT);
                    geocodeResult.addMessage("MapQuest status code: " + status);
                }

                JsonNode jsonResults = jsonNode.get("results");
                if (jsonResults.size() > 0){

                    JsonNode location = jsonResults.get(0).get("locations").get(0);

                    /** Build the address */
                    Address address = getAddressFromJson(location);

                    /** Build the geocode, in this case it's just the supplied point */
                    Geocode geocode = new Geocode();
                    geocode.setLatlon(point);
                    String qualityCode = location.get("geocodeQuality").asText();
                    geocode.setQuality((qualityMap.containsKey(qualityCode)) ? qualityMap.get(qualityCode) : GeocodeQuality.UNKNOWN);

                    geocodeResult.setGeocodedAddress(new GeocodedAddress(address, geocode));
                    return geocodeResult;
                }
            }
            catch(IOException ex){
                logger.error(ex.getMessage());
            }
            catch(NullPointerException ex){
                logger.error(ex.getMessage());
            }
        }
        return null;
    }

    /** Proxy to validate(addresses) */
    @Override
    public AddressResult validate(Address address)
    {
        ArrayList<AddressResult> addressResults = validate(new ArrayList<>(Arrays.asList(address)));
        return (addressResults != null) ? addressResults.get(0) : null;
    }

    /** MapQuest's geocoding service auto corrects addresses so that service can be extended here. */
    @Override
    public ArrayList<AddressResult> validate(ArrayList<Address> addresses)
    {
        ArrayList<GeocodeResult> geocodeResults = this.geocode(addresses);
        ArrayList<AddressResult> addressResults = new ArrayList<>();

        if (geocodeResults != null){
            for (GeocodeResult geocodeResult : geocodeResults) {

                AddressResult addressResult = new AddressResult(this.getClass());
                GeocodedAddress geocodedAddress = geocodeResult.getGeocodedAddress();

                /** If the quality is less than zip, it isn't really validated */
                boolean isValidated = false;
                if (geocodedAddress.getGeocode() != null){
                    if (geocodedAddress.getGeocode().getQuality().compareTo(ZIP) >= 0 ){
                        isValidated = true;
                    }
                }

                addressResult.setAddress(geocodedAddress.getAddress());
                addressResult.setValidated(isValidated);

                /** Append to address result list */
                addressResults.add(addressResult);
            }
            return addressResults;
        }
        return null;
    }

    /** No special functionality here. Just proxy to validate */
    @Override
    public AddressResult lookupCityState(Address address)
    {
        return validate(address);
    }

    /** No special functionality here. Just proxy to validate */
    @Override
    public ArrayList<AddressResult> lookupCityState(ArrayList<Address> addresses)
    {
        return validate(addresses);
    }

    /** No special functionality here. Just proxy to validate */
    @Override
    public AddressResult lookupZipCode(Address address)
    {
        return validate(address);
    }

    /** No special functionality here. Just proxy to validate */
    @Override
    public ArrayList<AddressResult> lookupZipCode(ArrayList<Address> addresses)
    {
        return validate(addresses);
    }

    /**
     * Create an address object by parsing the locations element of the json response.
     * @param location  A location node within the locations array the mapquest returns.
     * @return          Address
     */
    private Address getAddressFromJson(JsonNode location)
    {
        Address address = new Address();
        address.setAddr1(location.get("street").asText());
        address.setCity(location.get("adminArea5").asText());
        address.setState(location.get("adminArea3").asText());
        address.setPostal(location.get("postalCode").asText());
        return address;
    }
}
