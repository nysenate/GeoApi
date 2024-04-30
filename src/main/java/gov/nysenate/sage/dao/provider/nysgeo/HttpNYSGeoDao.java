package gov.nysenate.sage.dao.provider.nysgeo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.util.UrlRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.IOException;

@Repository
public class HttpNYSGeoDao {

    private static final Logger logger = LoggerFactory.getLogger(HttpNYSGeoDao.class);
    private final String DEFAULT_BASE_URL;

    private final String GEOCODE_EXTENSION;
    private String GEOCODE_QUERY = "?SingleLine=%s";


    private final String REV_GEOCODE_EXTENSION;
    private String REV_GEOCODE_QUERY = "?location={\"x\" : %s, \"y\" : %s, \"spatialReference\" : {\"wkid\" : 4326}}&returnIntersection=false";

    private final String COMMON_PARAMS = "&outSR=4326&f=pjson";
    private final ObjectMapper objectMapper = new ObjectMapper();



    @Autowired
    public HttpNYSGeoDao(Environment env) {
        this.DEFAULT_BASE_URL = env.getNysGeocoderUrl();
        this.GEOCODE_EXTENSION = env.getNysGeocdeExtension();
        this.REV_GEOCODE_EXTENSION = env.getNysRevGeocodeExtension();
    }

    /** {@inheritDoc} */
    public GeocodedAddress getGeocodedAddress(Address address)
    {
        GeocodedAddress geocodedAddress = null;

        try {
            String formattedQuery = String.format(GEOCODE_QUERY,address.getAddr1() + " " + address.getAddr2() + " "
                    + address.getPostalCity() + "," + address.getZip5());
            String url = DEFAULT_BASE_URL + GEOCODE_EXTENSION + formattedQuery + COMMON_PARAMS;
            geocodedAddress = getGeocodedAddress(url, false);
            if (geocodedAddress == null) {
                geocodedAddress = new GeocodedAddress(address);
            } else {
                geocodedAddress.setAddress(address);
            }

        }
        catch (NullPointerException ex) {
            logger.error("Null pointer while performing google geocode!", ex);
        }
        return geocodedAddress;
    }

    /** {@inheritDoc} */
    public GeocodedAddress getGeocodedAddress(Point point)
    {
        GeocodedAddress geocodedAddress = null;
        try {
            String formattedQuery = String.format(REV_GEOCODE_QUERY, point.getLon(), point.getLat());
            formattedQuery = formattedQuery.replaceAll(" ", "%20").replaceAll(" \" ","%22").replaceAll(",","%2C").replaceAll("\\{","%7B").replaceAll("}","%7D");
            logger.info(formattedQuery);
            String url = DEFAULT_BASE_URL + REV_GEOCODE_EXTENSION + formattedQuery + COMMON_PARAMS;
            geocodedAddress = getGeocodedAddress(url, true); // Response is identical to address->geocode response.
        }
        catch (NullPointerException ex) {
            logger.error("Null pointer while performing google geocode!", ex);
        }
        return geocodedAddress;
    }

    /** {@inheritDoc} */
    private GeocodedAddress getGeocodedAddress(String urlString, boolean isRevGeocode) {
        GeocodedAddress geocodedAddress = null;
        boolean resultParsed = false;

        try {
            String response = UrlRequest.getResponseFromUrl(urlString.replaceAll(" ", "%20"));
            if (response != null) {
                JsonNode node = objectMapper.readTree(response);
                double lat = 0.0;
                double lon = 0.0;
                int score = -1;
                Address address = null;

                if (isRevGeocode && node.has("address") && node.get("address") != null) {
                    JsonNode addressNode = node.get("address");
                    address = new Address(addressNode.get("Street").toString().trim().replaceAll("\"", ""),
                            addressNode.get("City").toString().trim().replaceAll("\"", ""),
                            addressNode.get("State").toString().trim().replaceAll("\"", ""),
                            addressNode.get("ZIP").toString().trim().replaceAll("\"", ""));
                    JsonNode location = node.get("location");
                    lon = location.get("x").asDouble();
                    lat = location.get("y").asDouble();
                    resultParsed = true;
                }
                else if (node.has("candidates") && node.get("candidates").get(0) != null) {
                    JsonNode candidate = node.get("candidates").get(0);
                    logger.trace(candidate.get("address").toString());

                    String[] candidateAddress = candidate.get("address").toString().split(",");
                    for (int i = 0; i < candidateAddress.length; i++) {
                        candidateAddress[i] = candidateAddress[i].trim().replaceAll("\"", "");
                    }

                    address = new Address(candidateAddress[0], candidateAddress[1], candidateAddress[2], candidateAddress[3]);

                    if (candidate.has("score") && candidate.get("score") != null) {
                        score = candidate.get("score").asInt();
                    }
                    JsonNode location = candidate.get("location");
                    lon = location.get("x").asDouble();
                    lat = location.get("y").asDouble();
                    resultParsed = true;
                }

                if (resultParsed) {
                    Geocode geocode = new Geocode( new Point(lat, lon),
                            resolveGeocodeQuality(score, isRevGeocode), HttpNYSGeoDao.class.getSimpleName());
                    geocodedAddress = new GeocodedAddress(address, geocode);
                }
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
