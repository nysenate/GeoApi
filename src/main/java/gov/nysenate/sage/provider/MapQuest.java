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
import gov.nysenate.sage.service.address.AddressService;
import gov.nysenate.sage.service.geo.GeocodeService;
import gov.nysenate.sage.util.Config;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.log4j.Logger;

/**
 * Comment this...
 * @author Graylin Kim, Ash Islam
 */
public class MapQuest implements AddressService, GeocodeService, Observer
{
    private static final String DEFAULT_BATCH_URL = "http://www.mapquestapi.com/geocoding/v1/batch";
    private static final String DEFAULT_REV_URL = "http://www.mapquestapi.com/geocoding/v1/reverse";
    private final Logger logger = Logger.getLogger(MapQuest.class);
    private Config config;

    private final int BATCH_SIZE = 95;
    private final HashMap<String, GeocodeQuality> qualityMap;
    private String geoBaseUrl;
    private String revBaseUrl;

    public MapQuest()
    {
        this.config = ApplicationFactory.getConfig();
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

        return;
    }

    /**
     *
     * @param address
     * @return
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
     *
     * @param addresses
     * @return
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

                Content content = Request.Get(url).execute().returnContent();
                String json = content.asString();

                logger.debug(json);

                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonNode = mapper.readTree(json);

                /** Get the status and kill the method if the status indicates a problem */
                String status = jsonNode.get("info").get("statuscode").asText();

                if (status != "0"){
                    logger.debug("MapQuest statuscode: " + status);
                    logger.debug("MapQuest messages " + jsonNode.get("info").get("messages"));
                    return null;
                }

                /** Fetch the results and translate them to GeocodeResult objects */
                JsonNode jsonResults = jsonNode.get("results");
                int numResults = jsonResults.size();

                for (int i = 0; i < numResults; i++){
                    GeocodedAddress geocodedAddress = new GeocodedAddress();
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
                        geocode.setQuality((qualityMap.containsKey(qualityCode)) ? qualityMap.get(qualityCode) : GeocodeQuality.UNKNOWN);
                    }

                    /** Build the response */
                    geocodedAddress.setAddress(addr);
                    geocodedAddress.setGeocode(geocode);
                    GeocodeResult geocodeResult = new GeocodeResult(geocodedAddress, status, this.getClass());
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
        if (point != null){
            /** Perform boxing */
            Double lat = point.getLatitude();
            Double lon = point.getLongitude();

            String url = revBaseUrl + "&lat=" + lat.toString() + "&lng=" + lon.toString();
            logger.debug(url);

            try {
                Content content = Request.Get(url).execute().returnContent();
                String json = content.asString();

                logger.debug(json);

                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonNode = mapper.readTree(json);

                /** Get the status and kill the method if the status indicates a problem */
                String status = jsonNode.get("info").get("statuscode").asText();
                if (status != "0"){
                    logger.error("MapQuest statuscode: " + status);
                    logger.error("MapQuest messages " + jsonNode.get("info").get("messages"));
                    return null;
                }

                JsonNode jsonResults = jsonNode.get("results");
                if (jsonResults.size() > 0){

                    JsonNode location = jsonResults.get(0).get("locations").get(0);
                    GeocodedAddress geocodedAddress = new GeocodedAddress();

                    /** Build the address */
                    Address address = getAddressFromJson(location);
                    geocodedAddress.setAddress(address);

                    /** Build the geocode, in this case it's just the supplied point */
                    Geocode geocode = new Geocode();
                    geocode.setLatlon(point);
                    String qualityCode = location.get("geocodeQuality").asText();
                    geocode.setQuality((qualityMap.containsKey(qualityCode)) ? qualityMap.get(qualityCode) : GeocodeQuality.UNKNOWN);

                    geocodedAddress.setAddress(address);
                    geocodedAddress.setGeocode(geocode);

                    return new GeocodeResult(geocodedAddress, "0", this.getClass());
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
            for (GeocodeResult geocodeResult : geocodeResults){

                GeocodedAddress geocodedAddress = geocodeResult.getGeocodedAddress();

                /** If the quality is less than zip, it isn't really validated */
                boolean isValidated = true;
                if (geocodedAddress.getGeocode().getQuality().compareTo(ZIP) < 0 ){
                    isValidated = false;
                }
                addressResults.add(new AddressResult(geocodedAddress.getAddress(), "0", this.getClass(), isValidated));
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

    private Address getAddressFromJson(JsonNode location) {
        Address address = new Address();
        address.setAddr1(location.get("street").asText());
        address.setCity(location.get("adminArea5").asText());
        address.setState(location.get("adminArea3").asText());

        /** Parse zip5-zip4 style postal code */
        String zip = location.get("postalCode").asText();
        ArrayList<String> zipParts = new ArrayList<>(Arrays.asList(zip.split("-")));

        address.setZip5((zipParts.size() > 0) ? zipParts.get(0) : "");
        address.setZip4((zipParts.size() > 1) ? zipParts.get(1) : "");
        return address;
    }
}
