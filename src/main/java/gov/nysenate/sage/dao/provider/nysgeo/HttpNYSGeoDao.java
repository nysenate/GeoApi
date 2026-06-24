package gov.nysenate.sage.dao.provider.nysgeo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.provider.geocode.Geocoder;
import gov.nysenate.sage.util.UrlRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Optional;

@Repository
public class HttpNYSGeoDao implements GeocoderDao {
    private static final Logger logger = LoggerFactory.getLogger(HttpNYSGeoDao.class);
    private static final String REV_GEOCODE_LOCATION = "{\"x\" : %s, \"y\" : %s, " +
            "\"spatialReference\" : {\"wkid\" : 4326}}";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${nys.geocoder.url:https://nysgeohub.ny.gov/arcgis/rest/services/Geocoder/NYS_Geocoder/GeocodeServer}")
    private String DEFAULT_BASE_URL;
    @Value("${nys.geocode.ext:/findAddressCandidates}")
    private String GEOCODE_EXTENSION;
    @Value("${nys.revgeocode.ext:/reverseGeocode}")
    private String REV_GEOCODE_EXTENSION;

    @Override
    public Geocoder geocoder() {
        return Geocoder.NYSGEO;
    }

    // TODO: use batching, suggested batch size is 1000
    /** {@inheritDoc} */
    public GeocodedAddress getGeocodedAddress(Address address) {
        if (!address.isValid()) {
            return null;
        }
        // TODO: use zipcode centroids?
        String url = UriComponentsBuilder.fromUriString(DEFAULT_BASE_URL + GEOCODE_EXTENSION)
                .queryParam("Address", address.getPrimaryAddr1())
                .queryParamIfPresent("City", Optional.ofNullable(StringUtils.trimToNull(address.getPostalCity())))
                .queryParamIfPresent("Region", Optional.ofNullable(StringUtils.trimToNull(address.getState())))
                .queryParamIfPresent("Postal", Optional.ofNullable(address.getZip5()))
                .queryParamIfPresent("PostalExt", Optional.ofNullable(address.getZip4()))
                .queryParam("outFields", "Addr_type,StAddr,City,Region,Postal,PostalExt")
                .queryParam("outSR", "4326")
                .queryParam("f", "pjson")
                .build().encode().toUriString();
        return getGeocodedAddress(url, false);
    }

    /** {@inheritDoc} */
    public GeocodedAddress getGeocodedAddress(Point point) {
        GeocodedAddress geocodedAddress = null;
        try {
            String location = String.format(REV_GEOCODE_LOCATION, point.lon(), point.lat());
            String url = UriComponentsBuilder.fromUriString(DEFAULT_BASE_URL + REV_GEOCODE_EXTENSION)
                    .queryParam("location", location)
                    .queryParam("returnIntersection", "false")
                    .queryParam("outSR", "4326")
                    .queryParam("f", "pjson")
                    .build().encode().toUriString();
            logger.debug(url);
            geocodedAddress = getGeocodedAddress(url, true); // Response is identical to address->geocode response.
        }
        catch (NullPointerException ex) {
            logger.error("Null pointer while performing {} geocode!", geocoder(), ex);
        }
        return geocodedAddress;
    }

    private GeocodedAddress getGeocodedAddress(String urlString, boolean isRevGeocode) {
        try {
            String response = UrlRequest.getResponseFromUrl(urlString);
            if (response == null) {
                return null;
            }
            JsonNode node = objectMapper.readTree(response);
            int score = -1;
            String addrType = null;
            Address address;

            if (isRevGeocode && node.has("address") && node.get("address") != null) {
                JsonNode addressNode = node.get("address");
                addrType = addressNode.path("Addr_type").asText();
                address = new Address(addressNode.path("Address").asText(),
                        addressNode.path("City").asText(),
                        addressNode.path("RegionAbbr").asText(),
                        addressNode.path("Postal").asText());
            }
            else if (node.has("candidates") && node.get("candidates").get(0) != null) {
                JsonNode bestNode = node.get("candidates").get(0);
                for (JsonNode candidateNode : node.get("candidates")) {
                    int currScore = candidateNode.get("score").asInt();
                    if (currScore > score) {
                        bestNode = candidateNode;
                        score = currScore;
                    }
                }
                node = bestNode;
                JsonNode attributes = node.path("attributes");
                addrType = attributes.path("Addr_type").asText();
                address = new Address(attributes.path("StAddr").asText(),
                        attributes.path("City").asText(),
                        attributes.path("Region").asText(),
                        attributes.path("Postal").asText());
            }
            else {
                return null;
            }

            JsonNode location = node.get("location");
            String lon = location.get("x").asText();
            String lat = location.get("y").asText();
            var geocode = new Geocode(new Point(lat, lon),
                    resolveGeocodeQuality(addrType), geocoder(), false);
            return new GeocodedAddress(address, geocode);
        }
        catch (IOException ex) {
            logger.error("Failed to retrieve data from NYS Geo api!", ex);
        }
        catch (NullPointerException ex) {
            logger.error("NullPointerException while parsing NYS Geocoder response!", ex);
        }
        logger.error("Bad query was: {}", urlString);
        return null;
    }

    /**
     * Determines the geocode quality from the {@code Addr_type} attribute, which classifies the kind
     * of feature the point represents (rooftop point, interpolated street address, postal centroid,
     * etc.). The new NYS geocoder returns {@code Addr_type} on both forward and reverse responses, so
     * the same mapping is used for either.
     *
     * @param addrType - the candidate's {@code Addr_type} attribute
     * @return geoQuality - the closest matching quality reference
     */
    private static GeocodeQuality resolveGeocodeQuality(String addrType) {
        return switch (addrType) {
            case "Subaddress", "PointAddress", "POI" -> GeocodeQuality.HOUSE;
            case "StreetAddress", "StreetInt", "StreetAddressExt", "DistanceMarker",
                 "StreetMidBlock", "StreetBetween", "StreetName" -> GeocodeQuality.STREET;
            case "Locality", "PostalLoc", "PostalExt", "Postal" -> GeocodeQuality.REGION;
            default -> {
                logger.warn("Unmapped NYSGeo Addr_type '{}'; defaulting to UNKNOWN quality.", addrType);
                yield GeocodeQuality.UNKNOWN;
            }
        };
    }
}
