package gov.nysenate.sage.provider;

import gov.nysenate.sage.dao.provider.DistrictShapefileDao;
import gov.nysenate.sage.model.address.DistrictedAddress;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictQuality;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.service.district.DistrictService;
import gov.nysenate.sage.service.district.DistrictServiceValidator;
import gov.nysenate.sage.service.district.ParallelDistrictService;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;

import static gov.nysenate.sage.service.district.DistrictServiceValidator.validateDistrictInfo;
import static gov.nysenate.sage.service.district.DistrictServiceValidator.validateInput;

public class DistrictShapefile implements DistrictService
{
    private static Logger logger = Logger.getLogger(Geoserver.class);
    private DistrictShapefileDao districtShapefileDao;
    private boolean fetchMaps = false;

    public DistrictShapefile()
    {
        this.districtShapefileDao = new DistrictShapefileDao();
    }

    @Override
    public boolean requiresGeocode() { return true; }

    @Override
    public boolean providesMaps() { return true; }

    @Override
    public void fetchMaps(boolean fetch) { this.fetchMaps = fetch; }

    @Override
    public DistrictResult assignDistricts(GeocodedAddress geocodedAddress)
    {
        return assignDistricts(geocodedAddress, DistrictType.getStateBasedTypes());
    }

    @Override
    public DistrictResult assignDistricts(GeocodedAddress geocodedAddress, List<DistrictType> reqTypes)
    {
        DistrictResult districtResult = new DistrictResult(this.getClass());

        /** Validate input */
        if (!validateInput(geocodedAddress, districtResult, true)) {
            return districtResult;
        }
        try {
            Geocode geocode = geocodedAddress.getGeocode();
            DistrictInfo districtInfo = this.districtShapefileDao.getDistrictInfo(geocode.getLatLon(), reqTypes, this.fetchMaps);

            /** Validate response */
            if (!validateDistrictInfo(districtInfo, reqTypes, districtResult)) {
                return districtResult;
            }
            /** Set the result. The quality here is always point since it's based of a geocode */
            districtResult.setDistrictedAddress(new DistrictedAddress(geocodedAddress, districtInfo, DistrictQuality.POINT));
        }
        catch (Exception ex) {
            districtResult.setStatusCode(ResultStatus.RESPONSE_PARSE_ERROR);
            logger.error(ex);
        }

        return districtResult;
    }

    @Override
    public List<DistrictResult> assignDistricts(List<GeocodedAddress> geocodedAddresses)
    {
        return assignDistricts(geocodedAddresses, DistrictType.getStandardTypes());
    }

    @Override
    public List<DistrictResult> assignDistricts(List<GeocodedAddress> geocodedAddresses, List<DistrictType> reqTypes)
    {
        return ParallelDistrictService.assignDistricts(this, geocodedAddresses, reqTypes);
    }

    @Override
    public Map<String, DistrictMap> nearbyDistricts(GeocodedAddress geocodedAddress, DistrictType districtType)
    {
        if (geocodedAddress != null && geocodedAddress.isGeocoded()) {
            Point point = geocodedAddress.getGeocode().getLatLon();
            return this.districtShapefileDao.getNearbyDistricts(districtType, point, 3);
        }
        return null;
    }
}
