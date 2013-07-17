package gov.nysenate.sage.provider;

import gov.nysenate.sage.dao.provider.GeoserverDao;
import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.address.DistrictedAddress;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictMatchLevel;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.service.district.DistrictService;
import gov.nysenate.sage.service.district.ParallelDistrictService;
import gov.nysenate.sage.util.Config;
import org.apache.log4j.Logger;

import java.util.*;

import static gov.nysenate.sage.service.district.DistrictServiceValidator.validateDistrictInfo;
import static gov.nysenate.sage.service.district.DistrictServiceValidator.validateInput;

/**
 * This class is an implementation of DistrictService to perform district assignment given a
 * geocoded address. GeoServer is a WMS/WFS server that provides an API to retrieve geo-spatial
 * and feature information from source data such as Census Shapefiles. Note that a coordinate pair
 * is required to perform district assignment using this implementation.
 */
public class Geoserver implements DistrictService, Observer
{
    private static Logger logger = Logger.getLogger(Geoserver.class);
    private GeoserverDao geoserverDao;
    private Config config;
    private boolean fetchMaps = false;

    public Geoserver()
    {
        this.geoserverDao = new GeoserverDao();
        this.config = ApplicationFactory.getConfig();
        this.config.notifyOnChange(this);
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
        this.geoserverDao.setBaseUrl(this.config.getValue("geoserver.url"));
        this.geoserverDao.setWorkspace(this.config.getValue("geoserver.workspace"));
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
            DistrictInfo districtInfo = this.geoserverDao.getDistrictInfo(geocode.getLatLon(), reqTypes);

            /** Validate response */
            if (!validateDistrictInfo(districtInfo, reqTypes, districtResult)) {
                return districtResult;
            }
            /** Set the result. The quality here is always point since it's based of a geocode */
            districtResult.setDistrictedAddress(new DistrictedAddress(geocodedAddress, districtInfo, DistrictMatchLevel.HOUSE));
        }
        catch (Exception ex) {
            districtResult.setStatusCode(ResultStatus.RESPONSE_PARSE_ERROR);
            logger.error(ex);
        }
        return districtResult;
    }

    @Override
    public List<DistrictResult> assignDistricts(List<GeocodedAddress> geocodedAddresses, List<DistrictType> reqTypes)
    {
        return ParallelDistrictService.assignDistricts(this, geocodedAddresses, reqTypes);
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
