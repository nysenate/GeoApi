package gov.nysenate.sage.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nysenate.sage.dao.CountyDao;
import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.address.DistrictedAddress;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.district.County;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.service.district.DistrictService;
import gov.nysenate.sage.util.Config;
import gov.nysenate.sage.util.UrlRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;

/**
 * This class is an implementation of DistrictService to perform district assignment given a
 * geocoded address. GeoServer is a WMS/WFS server that provides an API to retrieve geo-spatial
 * and feature information from source data such as Census Shapefiles. Note that a geocode is
 * required to perform district assignment using this implementation.
 *
 * Geoserver WFS Documentation : http://docs.geoserver.org/stable/en/user/services/wfs/reference.html
 */
public class Geoserver implements DistrictService, Observer
{
    private static final String DEFAULT_BASE_URL = "http://geoserver.nysenate.gov:8080/wfs";
    private static Logger logger = Logger.getLogger(Geoserver.class);
    private Config config;
    private ObjectMapper mapper;
    private String queryUrl;

    /** Mapping between fips codes and associated county */
    private Map<Integer, County> fipsCountyMap;

    public Geoserver()
    {
        this.config = ApplicationFactory.getConfig();
        this.config.notifyOnChange(this);
        this.mapper = new ObjectMapper();
        this.fipsCountyMap = new CountyDao().getFipsCountyMap();
        configure();
        logger.debug("Geoserver instantiated");
    }

    @Override
    public void update(Observable o, Object arg)
    {
        configure();
    }

    private void configure()
    {
        String baseUrl = this.config.getValue("geoserver.url");
        if (baseUrl.isEmpty()) {
            baseUrl = DEFAULT_BASE_URL;
        }
        queryUrl = baseUrl+"?service=WFS&version=1.1.0&request=GetFeature";
    }

    @Override
    public DistrictResult assignDistricts(GeocodedAddress geocodedAddress)
    {
        return assignDistricts(geocodedAddress, Arrays.asList(DistrictType.values()));
    }

    @Override
    public List<DistrictResult> assignDistricts(List<GeocodedAddress> geocodedAddresses)
    {
        return assignDistricts(geocodedAddresses, Arrays.asList(DistrictType.values()));
    }

    @Override
    public DistrictResult assignDistricts(GeocodedAddress geocodedAddress, List<DistrictType> types)
    {
        DistrictResult districtResult = new DistrictResult();
        districtResult.setSource(this.getClass());

        /** Proceed if the input is valid. Otherwise return the result with status code set. */
        if (!validateRequest(geocodedAddress, districtResult)) {
            logger.warn("Geocoded address could not be validated.");
            return districtResult;
        }

        try {
            DistrictInfo districtInfo = new DistrictInfo();
            Geocode geocode = geocodedAddress.getGeocode();
            String filter = String.format("INTERSECTS(the_geom, POINT ( %f %f ))", geocode.getLat(), geocode.getLon());
            JsonNode features = getFeatures(filter, types);

            /** Should only match one feature per layer as a point intersection */
            if (features.size() == 0) {
                districtResult.setStatusCode(ResultStatus.NO_DISTRICT_RESULT);
                districtResult.addMessage("No matching districts found at " + geocode.getLatLon().toString());
                return districtResult;
            }
            else if (features.size() > types.size()) {
                districtResult.setStatusCode(ResultStatus.MULTIPLE_DISTRICT_RESULT);
                districtResult.addMessage("Multiple matching features found for some layers");
                return districtResult;
            }

            for (int i = 0; i < features.size(); i++) {
                JsonNode feature = features.get(i);
                JsonNode properties = feature.get("properties");

                String layer = feature.get("id").asText().split("\\.")[0];
                if (layer.equals("school")) {
                    districtInfo.setSchoolName(properties.get("NAME").asText());
                    districtInfo.setSchoolCode(properties.get("TFCODE").asText());
                    districtResult.addAssignedDistrict(DistrictType.SCHOOL);
                }
                else if (layer.equals("town")) {
                    districtInfo.setTownName(properties.get("NAME").asText());
                    districtInfo.setTownCode(properties.get("ABBREV").asText());
                    districtResult.addAssignedDistrict(DistrictType.TOWN);
                }
                else if (layer.equals("election")) {
                    districtInfo.setElectionName(districtInfo.getSchoolCode());
                    districtInfo.setElectionCode(properties.get("ED").asInt());
                    districtResult.addAssignedDistrict(DistrictType.ELECTION);
                }
                else if (layer.equals("congressional")) {
                    districtInfo.setCongressionalName(properties.get("NAME").asText());
                    districtInfo.setCongressionalCode(properties.get("DISTRICT").asInt());
                    districtResult.addAssignedDistrict(DistrictType.CONGRESSIONAL);
                }
                else if (layer.equals("county")) {
                    districtInfo.setCountyName(properties.get("NAMELSAD").asText()); // NAME can also work
                    districtInfo.setCountyCode(fipsCountyMap.get(properties.get("COUNTYFP").asInt()).getId());
                    districtResult.addAssignedDistrict(DistrictType.COUNTY);
                }
                else if (layer.equals("assembly")) {
                    districtInfo.setAssemblyName(properties.get("NAME").asText());
                    districtInfo.setAssemblyCode(properties.get("DISTRICT").asInt());
                    districtResult.addAssignedDistrict(DistrictType.ASSEMBLY);
                }
                else if (layer.equals("senate")) {
                    districtInfo.setSenateName(properties.get("NAME").asText());
                    districtInfo.setSenateCode(properties.get("DISTRICT").asInt());
                    districtResult.addAssignedDistrict(DistrictType.SENATE);
                }
                else {
                    logger.warn("Unidentified feature id " + feature.get("id").asText() + " found in geoserver response");
                }
            }
            districtResult.setDistrictedAddress(new DistrictedAddress(geocodedAddress, districtInfo));
        }
        catch (Exception ex) {
            districtResult.setStatusCode(ResultStatus.RESPONSE_PARSE_ERROR);
            logger.error(ex);
        }
        return districtResult;
    }

    @Override
    public List<DistrictResult> assignDistricts(List<GeocodedAddress> geocodedAddresses, List<DistrictType> types)
    {
        ParallelDistrictService pds = new ParallelDistrictService();
        return pds.assignDistrictsParallel(this, geocodedAddresses, types);
    }

    /**
     * Submits a url request to GeoServer and retrieves a JsonNode containing the root 'features' element
     * @param filter    String specifying a point intersection filter
     * @param districtTypes    The district types to retrieve features from
     * @return JsonNode if successfull, null otherwise
     */
    private JsonNode getFeatures(String filter, List<DistrictType> districtTypes)
    {
        ArrayList<String> geotypes = new ArrayList<>();
        for (DistrictType districtType : districtTypes) {
            geotypes.add("nysenate:" + districtType.toString().toLowerCase());
        }
        String geotypeAttr = "typename="+ StringUtils.join(geotypes, ",");

        try {
            String sourceUrl = String.format(queryUrl +"&%s&CQL_FILTER=%s&outputformat=JSON", geotypeAttr, URLEncoder.encode(filter, "UTF-8"));
            String json = UrlRequest.getResponseFromUrl(sourceUrl);
            logger.debug(json);
            JsonNode root = mapper.readTree(json);

            JsonNode features = root.get("features");
            return features;
        }
        catch (IOException ex) {
            logger.error(ex.getMessage());
            return null;
        }
    }

    /**
     * Perform basic null checks on the input parameters.
     * @return true if all required objects are set, false otherwise
     */
    private boolean validateRequest(GeocodedAddress geoAddress, DistrictResult districtResult)
    {
        if (geoAddress == null) {
            districtResult.setStatusCode(ResultStatus.MISSING_INPUT_PARAMS);
        }
        else
        {
            if (geoAddress.getAddress() == null) {
                districtResult.setStatusCode(ResultStatus.MISSING_ADDRESS);
            }
            else if (geoAddress.getGeocode() == null) {
                districtResult.setStatusCode(ResultStatus.MISSING_GEOCODE);
            }
            else if (geoAddress.getGeocode().getLatLon() == null)
            {
                districtResult.setStatusCode(ResultStatus.INVALID_GEOCODE);
            }
            else {
                return true;
            }
        }
        return false;
    }
}
