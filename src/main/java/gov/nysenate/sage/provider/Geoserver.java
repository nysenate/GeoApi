package gov.nysenate.sage.provider;

import gov.nysenate.sage.dao.provider.GeoserverDao;
import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.address.DistrictedAddress;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.service.district.DistrictService;
import gov.nysenate.sage.service.district.ParallelDistrictService;
import gov.nysenate.sage.util.Config;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

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
            DistrictInfo districtInfo;
            Geocode geocode = geocodedAddress.getGeocode();
            districtInfo = this.geoserverDao.getDistrictInfo(geocode.getLatLon(), types);

            /** Check to see if districts were assigned */
            if (districtInfo == null || districtInfo.getAssignedDistricts().size() == 0) {
                districtResult.setStatusCode(ResultStatus.NO_DISTRICT_RESULT);
                districtResult.addMessage("No matching districts found at " + geocode.getLatLon().toString());
                return districtResult;
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
        return ParallelDistrictService.assignDistricts(this, geocodedAddresses, types);
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
