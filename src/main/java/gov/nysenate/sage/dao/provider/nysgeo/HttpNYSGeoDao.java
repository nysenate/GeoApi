package gov.nysenate.sage.dao.provider.nysgeo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nysenate.sage.model.address.BuildingAddress;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.provider.geocode.Geocoder;
import gov.nysenate.sage.util.UrlRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;

@Repository
public class HttpNYSGeoDao implements GeocoderDao {
    private static final Logger logger = LoggerFactory.getLogger(HttpNYSGeoDao.class);
    private static final String COMMON_PARAMS = "&outSR=4326&f=pjson";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${nys.geocoder.url:https://gisservices.its.ny.gov/arcgis/rest/services/Locators/Street_and_Address_Composite/GeocodeServer}")
    private String DEFAULT_BASE_URL;
    @Value("${nys.geocode.ext:/findAddressCandidates}")
    private String GEOCODE_EXTENSION;
    @Value("${nys.revgeocode.ext:/reverseGeocode}")
    private String REV_GEOCODE_EXTENSION;

    @Override
    public Geocoder geocoder() {
        return Geocoder.NYSGEO;
    }

    /** {@inheritDoc} */
    public GeocodedAddress getGeocodedAddress(BuildingAddress address) {
        String formattedQuery = String.format("?SingleLine=%s", address.getStreetWithNum() + " " + address.getInternal() + " "
                + address.getPostalCity() + "," + address.getZip5());
        String url = DEFAULT_BASE_URL + GEOCODE_EXTENSION + formattedQuery + COMMON_PARAMS;
        return getGeocodedAddress(url, false);
    }

    /** {@inheritDoc} */
    public GeocodedAddress getGeocodedAddress(Point point) {
        GeocodedAddress geocodedAddress = null;
        try {
            String REV_GEOCODE_QUERY = "?location={\"x\" : %s, \"y\" : %s, \"spatialReference\" : {\"wkid\" : 4326}}&returnIntersection=false";
            String formattedQuery = String.format(REV_GEOCODE_QUERY, point.lon(), point.lat());
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
        try {
            String response = UrlRequest.getResponseFromUrl(urlString.replaceAll(" ", "%20"));
            if (response == null) {
                return null;
            }
            JsonNode node = objectMapper.readTree(response);
            int score = -1;
            BuildingAddress address;

            if (isRevGeocode && node.has("address") && node.get("address") != null) {
                JsonNode addressNode = node.get("address");
                address = new BuildingAddress(addressNode.get("Street").toString().trim().replaceAll("\"", ""),
                        addressNode.get("City").toString().trim().replaceAll("\"", ""),
                        addressNode.get("State").toString().trim().replaceAll("\"", ""),
                        addressNode.get("ZIP").toString().trim().replaceAll("\"", ""));
            }
            else if (node.has("candidates") && node.get("candidates").get(0) != null) {
                node = node.get("candidates").get(0);

                String[] candidateAddress = node.get("address").toString().split(",");
                for (int i = 0; i < candidateAddress.length; i++) {
                    candidateAddress[i] = candidateAddress[i].trim().replaceAll("\"", "");
                }

                address = new BuildingAddress(candidateAddress[0], candidateAddress[1], candidateAddress[2], candidateAddress[3]);

                if (node.has("score") && node.get("score") != null) {
                    score = node.get("score").asInt();
                }
            }
            else {
                return null;
            }

            JsonNode location = node.get("location");
            String lon = location.get("x").asText();
            String lat = location.get("y").asText();
            var geocode = new Geocode(new Point(lat, lon),
                    resolveGeocodeQuality(score, isRevGeocode), Geocoder.GOOGLE.name());
            return new GeocodedAddress(address, geocode);
        }
        catch (IOException ex) {
            logger.error("Failed to retrieve data from NYS Geo api!", ex);
        }
        catch (NullPointerException ex) {
            logger.error("NullPointerException while parsing NYS Geocoder response!", ex);
        }
        return null;
    }

    /**
     * Determines the geocode quality of the geocode or the revgeocode
     * @param quality quality rating from json response
     * @param isRevGeocode - whether the request was a revgeocode or not
     * @return geoQuality - the closest matching quality reference
     */
    private static GeocodeQuality resolveGeocodeQuality(int quality, boolean isRevGeocode) {
        if (isRevGeocode) {
            return GeocodeQuality.UNKNOWN;
        }
        else if (quality == 100) {
            return GeocodeQuality.POINT;
        }
        else if (quality >= 90) {
            return GeocodeQuality.HOUSE;
        }
        else if (quality == 0){
            return GeocodeQuality.NOMATCH;
        }
        else {
            return GeocodeQuality.UNKNOWN;
        }
    }
}
