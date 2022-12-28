package gov.nysenate.sage.provider.district;

import com.google.common.collect.Sets;
import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.model.county.SqlCountyDao;
import gov.nysenate.sage.dao.provider.district.SqlDistrictShapefileDao;
import gov.nysenate.sage.dao.provider.streetfile.SqlStreetFileDao;
import gov.nysenate.sage.dao.provider.tiger.SqlTigerGeocoderDao;
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
import gov.nysenate.sage.provider.cityzip.CityZipDB;
import gov.nysenate.sage.service.district.ParallelDistrictService;
import gov.nysenate.sage.util.FormatUtil;
import gov.nysenate.sage.util.StreetAddressParser;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;

import static gov.nysenate.sage.model.result.ResultStatus.INSUFFICIENT_GEOCODE;
import static gov.nysenate.sage.service.district.DistrictServiceValidator.validateDistrictInfo;
import static gov.nysenate.sage.service.district.DistrictServiceValidator.validateInput;

@Service
public class DistrictShapefile implements DistrictService, MapService
{
    private static Logger logger = LoggerFactory.getLogger(DistrictShapefile.class);
    private final Environment env;
    private SqlDistrictShapefileDao sqlDistrictShapefileDao;

    /** The street file and cityzip daos are needed to determine overlap */
    private SqlStreetFileDao sqlStreetFileDao;
    private CityZipDB cityZipDBDao;
    private SqlTigerGeocoderDao sqlTigerGeocoderDao;
    private SqlCountyDao sqlCountyDao;
    private ParallelDistrictService parallelDistrictService;

    /** Specifies the maximum distance a neighbor district can be from a specific point to still be considered
     * a nearby neighbor. */
    private static Integer NEIGHBOR_PROXIMITY = 500;

    /** Specifies the maximum number of nearby neighbors that will be returned by default. */
    private static Integer MAX_NEIGHBORS = 2;

    /** We should only attempt to assign districts to a geocode if it is accurate enough.
     * i.e. We can't accurately assign a district to a ZIP, CITY, or STATE quality geocode. */
    private static final List<GeocodeQuality> DISTRICT_ASSIGNABLE_GEOCODE_QUALITIES =
            Arrays.asList(GeocodeQuality.HOUSE, GeocodeQuality.POINT);

    @Autowired
    public DistrictShapefile(SqlDistrictShapefileDao sqlDistrictShapefileDao, SqlStreetFileDao sqlStreetFileDao,
                             CityZipDB cityZipDB, SqlTigerGeocoderDao sqlTigerGeocoderDao, SqlCountyDao sqlCountyDao,
                             Environment env, ParallelDistrictService parallelDistrictService)
    {
        this.sqlDistrictShapefileDao = sqlDistrictShapefileDao;
        this.sqlStreetFileDao = sqlStreetFileDao;
        this.cityZipDBDao = cityZipDB;
        this.sqlTigerGeocoderDao = sqlTigerGeocoderDao;
        this.sqlCountyDao = sqlCountyDao;
        this.parallelDistrictService = parallelDistrictService;
        this.env = env;
        NEIGHBOR_PROXIMITY = env.getNeighborProximity();
        logger.debug("Instantiated DistrictShapefile.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean requiresGeocode() { return true; }

    /** {@inheritDoc} */
    public DistrictResult assignDistricts(GeocodedAddress geocodedAddress, List<DistrictType> reqTypes, boolean getSpecialMaps, boolean getProximity)
    {
        DistrictResult districtResult = new DistrictResult(this.getClass());

        /** Validate input */
        if (!validateInput(geocodedAddress, districtResult, true, false)) {
            return districtResult;
        }
        else if (!DISTRICT_ASSIGNABLE_GEOCODE_QUALITIES.contains(geocodedAddress.getGeocode().getQuality())) {
            districtResult.setStatusCode(INSUFFICIENT_GEOCODE);
            return districtResult;
        }
        try {
            Geocode geocode = geocodedAddress.getGeocode();
            DistrictInfo districtInfo = this.sqlDistrictShapefileDao.getDistrictInfo(geocode.getLatLon(), reqTypes, getSpecialMaps, getProximity);

            /** Validate response */
            if (!validateDistrictInfo(districtInfo, reqTypes, districtResult)) {
                return districtResult;
            }
            /** Set the result */
            districtResult.setDistrictedAddress(new DistrictedAddress(geocodedAddress, districtInfo, DistrictMatchLevel.HOUSE));
            districtResult.setResultTime(new Timestamp(new Date().getTime()));
            if (districtResult.getGeocodedAddress() != null) {
                logger.info(FormatUtil.toJsonString(districtResult.getGeocodedAddress()));
            }
            else {
                logger.info("The geocoded address was null");
            }
        }
        catch (Exception ex) {
            districtResult.setStatusCode(ResultStatus.RESPONSE_PARSE_ERROR);
            logger.error("" + ex);
        }

        return districtResult;
    }

    /** {@inheritDoc} */
    @Override
    public DistrictResult assignDistricts(GeocodedAddress geocodedAddress)
    {
        return assignDistricts(geocodedAddress, DistrictType.getStateBasedTypes());
    }

    /** {@inheritDoc} */
    @Override
    public DistrictResult assignDistricts(GeocodedAddress geocodedAddress, List<DistrictType> reqTypes)
    {
        return assignDistricts(geocodedAddress, reqTypes, true, true);
    }

    /** {@inheritDoc} */
    @Override
    public DistrictResult assignDistrictsForBatch(GeocodedAddress geocodedAddress, List<DistrictType> reqTypes)
    {
        return assignDistricts(geocodedAddress, reqTypes, false, false);
    }

    /** {@inheritDoc} */
    @Override
    public List<DistrictResult> assignDistricts(List<GeocodedAddress> geocodedAddresses)
    {
        return assignDistricts(geocodedAddresses, DistrictType.getStandardTypes());
    }

    /** {@inheritDoc} */
    @Override
    public List<DistrictResult> assignDistricts(List<GeocodedAddress> geocodedAddresses, List<DistrictType> reqTypes)
    {
        return parallelDistrictService.assignDistricts(this, geocodedAddresses, reqTypes);
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, DistrictMap> nearbyDistricts(GeocodedAddress geocodedAddress, DistrictType districtType)
    {
        return nearbyDistricts(geocodedAddress, districtType, MAX_NEIGHBORS);
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, DistrictMap> nearbyDistricts(GeocodedAddress geocodedAddress, DistrictType districtType, int count)
    {
        if (geocodedAddress != null && geocodedAddress.isValidGeocode()) {
            Point point = geocodedAddress.getGeocode().getLatLon();
            return this.sqlDistrictShapefileDao.getNearbyDistricts(districtType, point, true, NEIGHBOR_PROXIMITY, count);
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public MapResult getDistrictMap(DistrictType districtType, String code)
    {
        MapResult mapResult = new MapResult(this.getClass());
        if (code != null && !code.isEmpty()) {
            code = FormatUtil.trimLeadingZeroes(code);
            if (sqlDistrictShapefileDao.getDistrictMapLookup().get(districtType) != null) {
                DistrictMap map = sqlDistrictShapefileDao.getDistrictMapLookup().get(districtType).get(code);
                if (map != null) {
                    if (districtType.equals(DistrictType.COUNTY)) { //This if block is for the COVID19 links
                        map.setLink(sqlCountyDao.getCountyById(Integer.parseInt(code)).getLink());
                    }
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

    /** {@inheritDoc} */
    @Override
    public MapResult getDistrictMaps(DistrictType districtType)
    {
        MapResult mapResult = new MapResult(this.getClass());
        List<DistrictMap> mapCollection = sqlDistrictShapefileDao.getCachedDistrictMaps().get(districtType);
        if (mapCollection != null) {
            mapResult.setDistrictMaps(mapCollection);
            mapResult.setStatusCode(ResultStatus.SUCCESS);
            if (districtType.equals(DistrictType.COUNTY)) {
                for (DistrictMap map : mapCollection) {
                    map.setLink( sqlCountyDao.getCountyById( Integer.parseInt( map.getDistrictCode() )).getLink() );
                }
            }
        }
        else {
            mapResult.setStatusCode(ResultStatus.NO_MAP_RESULT);
        }
        return mapResult;
    }

    /**
     * Attempts to obtain overlapping district information when the geocoded address is ambiguous,
     * e.g represents the center of the city or zip area.
     * @param geocodedAddress GeocodedAddress
     * @return DistrictResult with overlaps and street ranges set.
     */
    public DistrictResult getMultiMatchResult(GeocodedAddress geocodedAddress, Boolean zipProvided)
    {
        DistrictResult districtResult = new DistrictResult(this.getClass());
        DistrictedAddress districtedAddress = new DistrictedAddress(geocodedAddress, null, DistrictMatchLevel.NOMATCH);
        DistrictInfo districtInfo = new DistrictInfo();
        DistrictMatchLevel matchLevel = DistrictMatchLevel.NOMATCH;
        ResultStatus resultStatus = ResultStatus.INSUFFICIENT_ADDRESS;

        /** Validate the geocoded address before proceeding */
        if (!validateInput(geocodedAddress, districtResult, true, true)) {
            return districtResult;
        }

        Address address = geocodedAddress.getAddress();
        GeocodeQuality geocodeQuality = geocodedAddress.getGeocode().getQuality();
        Map<DistrictType, Set<String>> matches;
        List<String> zip5List = new ArrayList<>();
        List<String> streetList = new ArrayList<>();

        logger.debug("Zip Provided: " + zipProvided);

        switch(geocodeQuality) {
            case NOMATCH: matchLevel = DistrictMatchLevel.NOMATCH;
                break;
            case STATE: matchLevel = DistrictMatchLevel.STATE;
                break;
            case COUNTY: matchLevel = DistrictMatchLevel.STATE;
                break;
            case CITY: matchLevel = DistrictMatchLevel.CITY;
                break;
            case ZIP: matchLevel = DistrictMatchLevel.ZIP5;
                break;
            case ZIP_EXT: matchLevel = DistrictMatchLevel.ZIP5;
                break;
            case STREET: matchLevel = DistrictMatchLevel.STREET;
                break;
            case HOUSE: matchLevel = DistrictMatchLevel.HOUSE;
                break;
            case POINT: matchLevel = DistrictMatchLevel.HOUSE;
                break;
        }
        if (zipProvided && matchLevel == DistrictMatchLevel.NOMATCH) {
            matchLevel = DistrictMatchLevel.ZIP5;
            geocodeQuality = GeocodeQuality.ZIP;

            Address reorderdAddress = StreetAddressParser.parseAddress(geocodedAddress.getAddress()).toAddress();
            geocodedAddress.setAddress(reorderdAddress);

        }
        districtedAddress.setDistrictMatchLevel(matchLevel);

        if (geocodeQuality.compareTo(GeocodeQuality.CITY) >= 0) { //40 quality or more
            if (geocodeQuality.compareTo(GeocodeQuality.ZIP) >= 0 &&!address.getZip5().isEmpty()) { //64 or more
                if (geocodeQuality.compareTo(GeocodeQuality.STREET) >= 0) { //72 or more
                    logger.debug("Determining street level district overlap");
                    streetList.add(address.getAddr1());
                    zip5List = (zipProvided) ? Arrays.asList(address.getZip5()) : cityZipDBDao.getZipsByCity(address.getCity());
                    districtInfo.setStreetLineReference(sqlTigerGeocoderDao.getStreetLineGeometry(address.getAddr1(), zip5List));
                    districtInfo.setStreetRanges(sqlStreetFileDao.getDistrictStreetRanges(address.getAddr1(), zip5List));
                }
                else {
                    logger.debug("Determining zip level district overlap");
                    zip5List = Arrays.asList(address.getZip5());
                }
            }
            else if (!address.getCity().isEmpty()) {
                logger.debug("Determining city level district overlap");
                zip5List = cityZipDBDao.getZipsByCity(address.getCity());
            }

            if (!zip5List.isEmpty()) {
                matches = sqlStreetFileDao.getAllStandardDistrictMatches(streetList, zip5List);
                if (matches != null && !matches.isEmpty()) {
                    /** Retrieve source map for city and zip match levels */
                    if (matchLevel.compareTo(DistrictMatchLevel.STREET) < 0) { //less than 64
                        DistrictMap sourceMap = sqlDistrictShapefileDao.getOverlapReferenceBoundary(DistrictType.ZIP, new HashSet<>(zip5List));
                        districtInfo.setReferenceMap(sourceMap);
                    }

                    for (DistrictType matchType : matches.keySet()) {
                        if (matches.get(matchType) != null && !matches.get(matchType).isEmpty() && !matchType.equals(DistrictType.ZIP)) {
                            Set<String> distCodeSet = matches.get(matchType);
                            DistrictOverlap overlap = null;
                            logger.trace("Matches for " + matchType + " " + distCodeSet);

                            /** Senate districts should always get overlap assigned */
                            if (matchType.equals(DistrictType.SENATE)) {
                                overlap = sqlDistrictShapefileDao.getDistrictOverlap(matchType, matches.get(matchType),
                                                                                  DistrictType.ZIP, new HashSet<>(zip5List));
                                districtInfo.addDistrictOverlap(matchType, overlap);
                            }
                            /** If the district set from the street files is size 1, set it as the district */
                            if (distCodeSet.size() == 1) {
                                districtInfo.setDistCode(matchType, distCodeSet.iterator().next());
                            }
                            /** Otherwise if the overlap count is size 1 for senate, set it as the district */
                            else if (matchType.equals(DistrictType.SENATE) && overlap != null && overlap.getTargetOverlap().size() == 1) {
                                districtInfo.setDistCode(matchType, overlap.getOverlapDistrictCodes().get(0));
                            }
                        }
                    }
                    resultStatus = ResultStatus.SUCCESS;
                    districtedAddress.setDistrictInfo(districtInfo);
                    districtedAddress.setDistrictMatchLevel(matchLevel);
                    logger.info("District match level: " + matchLevel);
                }
            }
        }
        districtResult.setStatusCode(resultStatus);
        districtResult.setDistrictedAddress(districtedAddress);
        districtResult.setResultTime(new Timestamp(new Date().getTime()));
        if (districtResult.getGeocodedAddress() != null) {
            logger.info(FormatUtil.toJsonString(districtResult.getGeocodedAddress()));
        }
        else {
            logger.info("The geocoded address was null");
        }
        return districtResult;
    }

    /**
     * Attempts to obtain overlapping district information for a specific district of arbitrary type.
     * @param districtType DistrictType the DistrictType of the district to get intersections with
     * @param districtId String the id of the district to get intersections with
     * @param intersectType DistrictType the type of district we are searching for intersections with districtId
     * @return DistrictResult with overlaps set.
     */
    public DistrictResult getIntersectionResult(DistrictType districtType, String districtId, DistrictType intersectType)
    {
        DistrictResult districtResult = new DistrictResult(this.getClass());
        // The match can always be set to the state level
        DistrictedAddress districtedAddress = new DistrictedAddress(null, null, DistrictMatchLevel.STATE);
        DistrictInfo districtInfo = new DistrictInfo();

        Map<DistrictType, Set<String>> matches;
        matches = sqlStreetFileDao.getAllIntersections(districtType, districtId);


        DistrictMap sourceMap = sqlDistrictShapefileDao.getOverlapReferenceBoundary(districtType, Sets.newHashSet(districtId));
        districtInfo.setReferenceMap(sourceMap);

        // We only need the overlap for the specified intersect type
        DistrictOverlap overlap = sqlDistrictShapefileDao.getDistrictOverlap(intersectType, matches.get(intersectType),
                districtType, Sets.newHashSet(districtId));
        districtInfo.addDistrictOverlap(intersectType, overlap);

        districtedAddress.setDistrictInfo(districtInfo);
        districtResult.setDistrictedAddress(districtedAddress);

        return districtResult;
    }
}