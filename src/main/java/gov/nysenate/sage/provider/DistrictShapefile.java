package gov.nysenate.sage.provider;

import gov.nysenate.sage.dao.provider.DistrictShapefileDao;
import gov.nysenate.sage.dao.provider.StreetFileDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.DistrictedAddress;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.district.*;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.model.result.MapResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.service.district.DistrictService;
import gov.nysenate.sage.service.district.ParallelDistrictService;
import gov.nysenate.sage.service.map.MapService;
import gov.nysenate.sage.util.FormatUtil;
import org.apache.log4j.Logger;

import java.util.*;

import static gov.nysenate.sage.service.district.DistrictServiceValidator.validateDistrictInfo;
import static gov.nysenate.sage.service.district.DistrictServiceValidator.validateInput;

public class DistrictShapefile implements DistrictService, MapService
{
    private static Logger logger = Logger.getLogger(DistrictShapefile.class);
    private DistrictShapefileDao districtShapefileDao;

    /** The street file and cityzip daos are needed to determine overlap */
    private StreetFileDao streetFileDao;
    private CityZipDB cityZipDBDao;

    public DistrictShapefile()
    {
        this.districtShapefileDao = new DistrictShapefileDao();
        this.streetFileDao = new StreetFileDao();
        this.cityZipDBDao = new CityZipDB();
    }

    @Override
    public boolean requiresGeocode() { return true; }

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
        if (!validateInput(geocodedAddress, districtResult, true, false)) {
            return districtResult;
        }
        try {
            Geocode geocode = geocodedAddress.getGeocode();
            DistrictInfo districtInfo = this.districtShapefileDao.getDistrictInfo(geocode.getLatLon(), reqTypes, true);

            /** Validate response */
            if (!validateDistrictInfo(districtInfo, reqTypes, districtResult)) {
                return districtResult;
            }
            /** Set the result. The quality here is always point since it's based of a geocode */
            districtResult.setDistrictedAddress(new DistrictedAddress(geocodedAddress, districtInfo, DistrictMatchLevel.POINT));
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
        return nearbyDistricts(geocodedAddress, districtType, 2);
    }

    @Override
    public Map<String, DistrictMap> nearbyDistricts(GeocodedAddress geocodedAddress, DistrictType districtType, int count)
    {
        if (geocodedAddress != null && geocodedAddress.isValidGeocode()) {
            Point point = geocodedAddress.getGeocode().getLatLon();
            return this.districtShapefileDao.getNearbyDistricts(districtType, point, true, count);
        }
        return null;
    }


    @Override
    public MapResult getDistrictMap(DistrictType districtType, String code)
    {
        MapResult mapResult = new MapResult(this.getClass());
        if (code != null && !code.isEmpty()) {
            code = FormatUtil.trimLeadingZeroes(code);
            if (districtShapefileDao.getDistrictMapLookup().get(districtType) != null) {
                DistrictMap map = districtShapefileDao.getDistrictMapLookup().get(districtType).get(code);
                if (map != null) {
                    mapResult.setDistrictMap(map);
                    mapResult.setStatusCode(ResultStatus.SUCCESS);
                }
                else {
                    mapResult.setStatusCode(ResultStatus.NO_MAP_RESULT);
                }
            }
            else {
                mapResult.setStatusCode(ResultStatus.UNSUPPORTED_DISTRICT_MAP);
            }
        }
        else {
            mapResult.setStatusCode(ResultStatus.MISSING_DISTRICT_CODE);
        }
        return mapResult;
    }

    @Override
    public MapResult getDistrictMaps(DistrictType districtType)
    {
        MapResult mapResult = new MapResult(this.getClass());
        List<DistrictMap> mapCollection = districtShapefileDao.getDistrictMaps().get(districtType);
        if (mapCollection != null) {
            mapResult.setDistrictMaps(mapCollection);
            mapResult.setStatusCode(ResultStatus.SUCCESS);
        }
        else {
            mapResult.setStatusCode(ResultStatus.NO_MAP_RESULT);
        }
        return mapResult;
    }

    /**
     *
     * @param geocodedAddress
     * @return
     */
    public DistrictResult getOverlapDistrictResult(GeocodedAddress geocodedAddress)
    {
        DistrictResult districtResult = new DistrictResult(this.getClass());
        DistrictedAddress districtedAddress = new DistrictedAddress(geocodedAddress, null, DistrictMatchLevel.NOMATCH);
        DistrictInfo districtInfo = new DistrictInfo();

        /** Validate the geocoded address before proceeding */
        if (!validateInput(geocodedAddress, districtResult, true, true)) {
            return districtResult;
        }

        Address address = geocodedAddress.getAddress();
        GeocodeQuality geocodeQuality = geocodedAddress.getGeocode().getQuality();

        if (geocodeQuality.compareTo(GeocodeQuality.STREET) >= 0) {
            logger.debug("Determining street level district overlap");
            districtedAddress.setDistrictMatchLevel(DistrictMatchLevel.STREET);
            return districtResult;
        }
        else if (geocodeQuality.compareTo(GeocodeQuality.CITY) >= 0) {
            Map<DistrictType, Set<String>> matches = new HashMap<>();
            List<String> zip5List = new ArrayList<>();

            if (geocodeQuality.compareTo(GeocodeQuality.ZIP) >= 0 &&!address.getZip5().isEmpty()) {
                logger.debug("Determining zip level district overlap");
                districtedAddress.setDistrictMatchLevel(DistrictMatchLevel.ZIP5);
                zip5List = Arrays.asList(address.getZip5());
            }
            else if (!address.getCity().isEmpty()) {
                logger.debug("Determining city level district overlap");
                districtedAddress.setDistrictMatchLevel(DistrictMatchLevel.CITY);
                zip5List = cityZipDBDao.getZipsByCity(address.getCity());
            }

            if (!zip5List.isEmpty()) {
                matches = streetFileDao.getAllStandardDistrictMatches(zip5List);
                if (matches != null && !matches.isEmpty()) {
                    /** Retrieve source map */
                    DistrictMap sourceMap = districtShapefileDao.getOverlapReferenceBoundary(DistrictType.ZIP, new HashSet<String>(zip5List));
                    districtInfo.setReferenceMap(sourceMap);

                    for (DistrictType matchType : matches.keySet()) {
                        if (matches.get(matchType) != null && !matches.get(matchType).isEmpty() && !matchType.equals(DistrictType.ZIP)) {
                            Set<String> distCodeSet = matches.get(matchType);
                            logger.debug("Matches for " + matchType + " " + distCodeSet);
                            if (distCodeSet.size() == 1) {
                                logger.debug("Setting as district");
                                districtInfo.setDistCode(matchType, distCodeSet.iterator().next());
                            }
                            else {
                                DistrictOverlap overlap = districtShapefileDao.getDistrictOverlap(matchType, matches.get(matchType),
                                        DistrictType.ZIP, new HashSet<String>(zip5List));
                                if (overlap.getTargetOverlap().size() == 1) {
                                    districtInfo.setDistCode(matchType, overlap.getOverlapDistrictCodes().get(0));
                                }
                                else {
                                    logger.debug("Setting as overlap");
                                    districtInfo.addDistrictOverlap(matchType, overlap);
                                }
                            }
                        }
                    }
                    districtResult.setStatusCode(ResultStatus.MULTIPLE_DISTRICT_RESULT);
                    districtedAddress.setDistrictInfo(districtInfo);
                    districtResult.setDistrictedAddress(districtedAddress);
                    return districtResult;
                }
            }
        }
        districtResult.setStatusCode(ResultStatus.INSUFFICIENT_ADDRESS);
        districtResult.setGeocodedAddress(geocodedAddress);
        return districtResult;
    }
}
