package gov.nysenate.sage.provider.district;

import gov.nysenate.sage.dao.model.county.SqlCountyDao;
import gov.nysenate.sage.dao.provider.district.SqlDistrictShapefileDao;
import gov.nysenate.sage.dao.provider.streetfile.SqlStreetfileDao;
import gov.nysenate.sage.dao.provider.tiger.TigerDao;
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
import gov.nysenate.sage.util.FormatUtil;
import gov.nysenate.sage.util.NonnullList;
import gov.nysenate.sage.util.StreetAddressParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static gov.nysenate.sage.model.result.ResultStatus.INSUFFICIENT_GEOCODE;
import static gov.nysenate.sage.model.result.ResultStatus.SUCCESS;

@Service
public class DistrictShapefile extends DistrictService implements MapService {
    private static final Logger logger = LoggerFactory.getLogger(DistrictShapefile.class);
    private final SqlDistrictShapefileDao sqlDistrictShapefileDao;

    /** The street file and cityzip daos are needed to determine overlap */
    private final SqlStreetfileDao sqlStreetFileDao;
    private final CityZipDB cityZipDBDao;
    private final TigerDao tigerDao;
    private final SqlCountyDao sqlCountyDao;

    /** Specifies the maximum distance a neighbor district can be from a specific point to still be considered
     * a nearby neighbor. */
    @Value("${neighbor.proximity:500}")
    private int neighborProximity;

    /** Specifies the maximum number of nearby neighbors that will be returned by default. */
    private static Integer MAX_NEIGHBORS = 2;

    /** We should only attempt to assign districts to a geocode if it is accurate enough.
     * i.e. We can't accurately assign a district to a ZIP, CITY, or STATE quality geocode. */
    private static final List<GeocodeQuality> DISTRICT_ASSIGNABLE_GEOCODE_QUALITIES =
            Arrays.asList(GeocodeQuality.HOUSE, GeocodeQuality.POINT);

    @Autowired
    public DistrictShapefile(SqlDistrictShapefileDao sqlDistrictShapefileDao, SqlStreetfileDao sqlStreetFileDao,
                             CityZipDB cityZipDB, TigerDao tigerDao, SqlCountyDao sqlCountyDao) {
        this.sqlDistrictShapefileDao = sqlDistrictShapefileDao;
        this.sqlStreetFileDao = sqlStreetFileDao;
        this.cityZipDBDao = cityZipDB;
        this.tigerDao = tigerDao;
        this.sqlCountyDao = sqlCountyDao;
        logger.debug("Instantiated DistrictShapefile.");
    }

    /** {@inheritDoc} */
    public DistrictResult assignDistricts(GeocodedAddress geocodedAddress, List<DistrictType> reqTypes, boolean getSpecialMaps, boolean getProximity) {
        var districtResult = new DistrictResult(districtSource(), geocodedAddress, true, false);
        if (!districtResult.isSuccess()) {
            return districtResult;
        }
        if (!DISTRICT_ASSIGNABLE_GEOCODE_QUALITIES.contains(geocodedAddress.getGeocode().quality())) {
            districtResult.setStatusCode(INSUFFICIENT_GEOCODE);
            return districtResult;
        }
        try {
            Geocode geocode = geocodedAddress.getGeocode();
            DistrictInfo districtInfo = sqlDistrictShapefileDao.getDistrictInfo(geocode.point(), reqTypes, getSpecialMaps, getProximity);
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
            logger.error("{}", String.valueOf(ex));
        }

        return districtResult;
    }

    @Override
    public DistrictSource districtSource() {
        return DistrictSource.SHAPEFILE;
    }

    /** {@inheritDoc} */
    @Override
    public DistrictResult assignDistricts(GeocodedAddress geocodedAddress, List<DistrictType> reqTypes) {
        return assignDistricts(geocodedAddress, reqTypes, true, true);
    }

    /** {@inheritDoc} */
    @Override
    public DistrictResult assignDistrictsForBatch(GeocodedAddress geocodedAddress, List<DistrictType> reqTypes) {
        return assignDistricts(geocodedAddress, reqTypes, false, false);
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, DistrictMap> nearbyDistricts(GeocodedAddress geocodedAddress, DistrictType districtType) {
        return nearbyDistricts(geocodedAddress, districtType, MAX_NEIGHBORS);
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, DistrictMap> nearbyDistricts(GeocodedAddress geocodedAddress, DistrictType districtType, int count) {
        if (geocodedAddress != null && geocodedAddress.isValidGeocode()) {
            Point point = geocodedAddress.getGeocode().point();
            return this.sqlDistrictShapefileDao.getNearbyDistricts(districtType, point, true, neighborProximity, count);
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public MapResult getDistrictMap(DistrictType districtType, String code) {
        MapResult mapResult = new MapResult(MapSource.SHAPEFILE);
        if (code != null && !code.isEmpty()) {
            code = FormatUtil.trimLeadingZeroes(code);
            var strToDistMap = sqlDistrictShapefileDao.getCodeToDistrictMapMap(districtType);
            if (strToDistMap != null) {
                DistrictMap map = strToDistMap.get(code);
                if (map != null) {
                    if (districtType.equals(DistrictType.COUNTY)) { //This if block is for the COVID19 links
                        map.setLink(sqlCountyDao.getCountyById(Integer.parseInt(code)).link());
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
    public MapResult getDistrictMaps(DistrictType districtType) {
        MapResult mapResult = new MapResult(MapSource.SHAPEFILE);
        List<DistrictMap> mapCollection = sqlDistrictShapefileDao.getDistrictMaps(districtType);
        if (mapCollection != null) {
            mapResult.setDistrictMaps(mapCollection);
            mapResult.setStatusCode(ResultStatus.SUCCESS);
            if (districtType.equals(DistrictType.COUNTY)) {
                for (DistrictMap map : mapCollection) {
                    map.setLink(sqlCountyDao.getCountyById( Integer.parseInt( map.getDistrictCode() )).link());
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
    public DistrictResult getMultiMatchResult(GeocodedAddress geocodedAddress, boolean zipProvided) {
        var districtResult = new DistrictResult(districtSource(), geocodedAddress, true, true);
        if (!districtResult.isSuccess()) {
            return districtResult;
        }
        var districtedAddress = new DistrictedAddress(geocodedAddress, null, DistrictMatchLevel.NOMATCH);
        var districtInfo = new DistrictInfo();
        var resultStatus = ResultStatus.INSUFFICIENT_ADDRESS;

        Address address = geocodedAddress.getAddress();
        GeocodeQuality geocodeQuality = geocodedAddress.getGeocode().quality();
        Map<DistrictType, Set<String>> matches;
        List<Integer> zip5List = new ArrayList<>();
        List<String> streetList = new ArrayList<>();

        logger.debug("Zip Provided: {}", zipProvided);

        DistrictMatchLevel matchLevel = switch (geocodeQuality) {
            case STATE, COUNTY -> DistrictMatchLevel.STATE;
            case CITY -> DistrictMatchLevel.CITY;
            case ZIP, ZIP_EXT -> DistrictMatchLevel.ZIP5;
            case STREET -> DistrictMatchLevel.STREET;
            case HOUSE, POINT -> DistrictMatchLevel.HOUSE;
            default -> DistrictMatchLevel.NOMATCH;
        };
        if (zipProvided && matchLevel == DistrictMatchLevel.NOMATCH) {
            matchLevel = DistrictMatchLevel.ZIP5;
            geocodeQuality = GeocodeQuality.ZIP;

            Address reorderdAddress = StreetAddressParser.parseAddress(geocodedAddress.getAddress()).toAddress();
            geocodedAddress.setAddress(reorderdAddress);
        }
        districtedAddress.setDistrictMatchLevel(matchLevel);

        if (geocodeQuality.compareTo(GeocodeQuality.CITY) >= 0) { //40 quality or more
            if (geocodeQuality.compareTo(GeocodeQuality.ZIP) >= 0 && address.getZip5() != null) { //64 or more
                if (geocodeQuality.compareTo(GeocodeQuality.STREET) >= 0) { //72 or more
                    logger.debug("Determining street level district overlap");
                    streetList.add(address.getAddr1());
                    zip5List = (zipProvided) ? List.of(address.getZip5()) : cityZipDBDao.getZipsByCity(address.getPostalCity());
                    districtInfo.setStreetLineReference(tigerDao.getStreetLineGeometry(address.getAddr1(), zip5List));
                    districtInfo.setStreetRanges(sqlStreetFileDao.getDistrictStreetRanges(address.getAddr1(), zip5List));
                }
                else {
                    logger.debug("Determining zip level district overlap");
                    zip5List = List.of(address.getZip5());
                }
            }
            else if (!address.getPostalCity().isEmpty()) {
                logger.debug("Determining city level district overlap");
                zip5List = cityZipDBDao.getZipsByCity(address.getPostalCity());
            }

            if (!zip5List.isEmpty()) {
                matches = sqlStreetFileDao.getAllStandardDistrictMatches(streetList, NonnullList.of(zip5List));
                if (matches != null && !matches.isEmpty()) {
                    Set<String> zip5Set = zip5List.stream().map(Object::toString).collect(Collectors.toSet());
                    /** Retrieve source map for city and zip match levels */
                    if (matchLevel.compareTo(DistrictMatchLevel.STREET) < 0) { //less than 64
                        DistrictMap sourceMap = sqlDistrictShapefileDao.getOverlapReferenceBoundary(DistrictType.ZIP, zip5Set);
                        districtInfo.setReferenceMap(sourceMap);
                    }

                    for (DistrictType matchType : matches.keySet()) {
                        if (matches.get(matchType) != null && !matches.get(matchType).isEmpty() && !matchType.equals(DistrictType.ZIP)) {
                            Set<String> distCodeSet = matches.get(matchType);
                            DistrictOverlap overlap = null;
                            logger.trace("Matches for {} {}", matchType, distCodeSet);

                            /** Senate districts should always get overlap assigned */
                            if (matchType.equals(DistrictType.SENATE)) {
                                overlap = sqlDistrictShapefileDao.getDistrictOverlap(matchType, matches.get(matchType),
                                        DistrictType.ZIP, zip5Set);
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
                    logger.info("District match level: {}", matchLevel);
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
    public DistrictResult getIntersectionResult(DistrictType districtType, String districtId, DistrictType intersectType) {
        // The match can always be set to the state level
        DistrictedAddress districtedAddress = new DistrictedAddress(null, null, DistrictMatchLevel.STATE);
        DistrictInfo districtInfo = new DistrictInfo();

        Map<DistrictType, Set<String>> matches = sqlStreetFileDao.getAllIntersections(districtType, districtId);

        DistrictMap sourceMap = sqlDistrictShapefileDao.getOverlapReferenceBoundary(districtType, Set.of(districtId));
        districtInfo.setReferenceMap(sourceMap);

        // We only need the overlap for the specified intersect type
        DistrictOverlap overlap = sqlDistrictShapefileDao.getDistrictOverlap(intersectType, matches.get(intersectType),
                districtType, Set.of(districtId));
        districtInfo.addDistrictOverlap(intersectType, overlap);

        districtedAddress.setDistrictInfo(districtInfo);
        var districtResult = new DistrictResult(districtSource(), null, SUCCESS);
        districtResult.setDistrictedAddress(districtedAddress);

        return districtResult;
    }
}
