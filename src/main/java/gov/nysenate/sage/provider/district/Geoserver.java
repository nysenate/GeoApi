package gov.nysenate.sage.provider.district;

import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.provider.geoserver.HttpGeoserverDao;
import gov.nysenate.sage.model.address.DistrictedAddress;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictMatchLevel;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.service.district.ParallelDistrictService;
import org.springframework.stereotype.Service;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;

import static gov.nysenate.sage.service.district.DistrictServiceValidator.validateDistrictInfo;
import static gov.nysenate.sage.service.district.DistrictServiceValidator.validateInput;

/**
 * This class is an implementation of DistrictService to perform district assignment given a
 * geocoded address. GeoServer is a WMS/WFS server that provides an API to retrieve geo-spatial
 * and feature information from source data such as Census Shapefiles. Note that a coordinate pair
 * is required to perform district assignment using this implementation.
 */
@Service
public class Geoserver implements DistrictService
{
    private static Logger logger = LoggerFactory.getLogger(Geoserver.class);
    private HttpGeoserverDao httpGeoserverDao;
    private boolean fetchMaps = false;
    private BaseDao baseDao;
    private Environment env;

    private ParallelDistrictService parallelDistrictService;

    public Geoserver(HttpGeoserverDao httpGeoserverDao, BaseDao baseDao, ParallelDistrictService parallelDistrictService,
                     Environment env)
    {
        this.env = env;
        this.httpGeoserverDao = httpGeoserverDao;
        this.baseDao = baseDao;
        this.parallelDistrictService = parallelDistrictService;
        logger.debug("Geoserver instantiated");
    }

    @Override
    public boolean requiresGeocode() { return true; }

    @Override
    public DistrictResult assignDistricts(GeocodedAddress geocodedAddress)
    {
        return assignDistricts(geocodedAddress, DistrictType.getStandardTypes());
    }

    @Override
    public List<DistrictResult> assignDistricts(List<GeocodedAddress> geocodedAddresses)
    {
        return assignDistricts(geocodedAddresses, DistrictType.getStandardTypes());
    }

    @Override
    public DistrictResult assignDistricts(GeocodedAddress geocodedAddress, List<DistrictType> reqTypes)
    {
        DistrictResult districtResult = new DistrictResult(this.getClass());

        /** Validate input */
        if (!validateInput(geocodedAddress, districtResult, true, false)) {
            return districtResult;
        }
        try {
            Geocode geocode = geocodedAddress.getGeocode();
            DistrictInfo districtInfo = this.httpGeoserverDao.getDistrictInfo(geocode.getLatLon(), reqTypes);

            /** Validate response */
            if (!validateDistrictInfo(districtInfo, reqTypes, districtResult)) {
                return districtResult;
            }
            /** Set the result. The quality here is always point since it's based of a geocode */
            districtResult.setDistrictedAddress(new DistrictedAddress(geocodedAddress, districtInfo, DistrictMatchLevel.HOUSE));
        }
        catch (Exception ex) {
            districtResult.setStatusCode(ResultStatus.RESPONSE_PARSE_ERROR);
            logger.error("" + ex);
        }
        return districtResult;
    }

    @Override
    public List<DistrictResult> assignDistricts(List<GeocodedAddress> geocodedAddresses, List<DistrictType> reqTypes)
    {
        return parallelDistrictService.assignDistricts(this, geocodedAddresses, reqTypes);
    }

    @Override
    public DistrictResult assignDistrictsForBatch(GeocodedAddress geocodedAddress, List<DistrictType> reqTypes) {
        return assignDistricts(geocodedAddress, reqTypes);
    }

    @Override
    public Map<String, DistrictMap> nearbyDistricts(GeocodedAddress geocodedAddress, DistrictType districtType) {
        logger.warn("Nearby districts is not implemented in Geoserver");
        return null;
    }

    @Override
    public Map<String, DistrictMap> nearbyDistricts(GeocodedAddress geocodedAddress, DistrictType districtType, int count) {
        logger.warn("Nearby districts is not implemented in Geoserver");
        return null;
    }
}
