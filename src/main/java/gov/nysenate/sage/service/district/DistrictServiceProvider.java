package gov.nysenate.sage.service.district;

import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.factory.SageThreadFactory;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.api.BatchDistrictRequest;
import gov.nysenate.sage.model.api.DistrictRequest;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.provider.DistrictShapefile;
import gov.nysenate.sage.provider.Geoserver;
import gov.nysenate.sage.provider.StreetFile;
import gov.nysenate.sage.service.base.ServiceProviders;
import gov.nysenate.sage.util.Config;
import gov.nysenate.sage.util.FormatUtil;
import gov.nysenate.sage.util.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.*;

/**
 * Point of access for all district assignment requests. This class maintains a collection of available
 * district providers and contains logic for distributing requests and collecting responses from the providers.
 */
@Service
public class DistrictServiceProvider extends ServiceProviders<DistrictService> implements Observer
{
    public enum DistrictStrategy {
        neighborMatch,  /** Perform shape and street lookup, performing neighbor consolidation as needed. */
        streetFallback, /** Perform shape lookup and consolidate street file results without neighbor checking. */
        shapeFallback,  /** Perform street lookup and only fall back to shape files when street lookup failed. */
        streetOnly,     /** Perform street lookup only */
        shapeOnly       /** Perform shape lookup only */
    }

    private final Environment env;
    private final Logger logger = LoggerFactory.getLogger(DistrictServiceProvider.class);

    private static DistrictStrategy SINGLE_DISTRICT_STRATEGY = DistrictStrategy.valueOf("neighborMatch");
    private static DistrictStrategy BATCH_DISTRICT_STRATEGY = DistrictStrategy.valueOf("shapeFallback");

    /** Specifies the distance (meters) to a district boundary in which the accuracy of shapefiles is uncertain */
    private static Integer PROXIMITY_THRESHOLD = 300;

    /** Specifies the set of districts that are allowed to obtain nearby neighbor info. The reason every
     * district that has shape files can't do this is because the query can be rather slow. */
    private static final Set<DistrictType> allowNeighborAssignSet = new HashSet<>();
    static {
        allowNeighborAssignSet.add(DistrictType.SENATE);
    }

    @Autowired
    public DistrictServiceProvider(Environment env)
    {
        this.env = env;

        registerDefaultProvider("shapefile", DistrictShapefile.class);
        registerProvider("streetfile", StreetFile.class);
        registerProvider("geoserver", Geoserver.class);
        setProviderFallbackChain(Arrays.asList("streetfile"));
    }

    @Override
    public void update(Observable o, Object arg)
    {
        PROXIMITY_THRESHOLD = env.getBorderProximity();
        SINGLE_DISTRICT_STRATEGY = DistrictStrategy.valueOf(env.getDistrictStrategySingle());
        BATCH_DISTRICT_STRATEGY = DistrictStrategy.valueOf(env.getDistrictStrategyBatch());
    }

    /** Single District Assign ---------------------------------------------------------------------------------------*/

    /**
     * Assign districts using supplied DistrictRequest.
     * @param geocodedAddress
     * @param districtRequest
     * @return DistrictResult
     */
    public DistrictResult assignDistricts(final GeocodedAddress geocodedAddress, final DistrictRequest districtRequest)
    {
        if (districtRequest != null) {
            return assignDistricts(geocodedAddress, districtRequest.getProvider(), districtRequest.getDistrictTypes(), districtRequest.getDistrictStrategy());
        }
        return null;
    }

    /**
     * Assign standard districts using default method.
     * @param geocodedAddress
     * @return DistrictResult
     */
    public DistrictResult assignDistricts(final GeocodedAddress geocodedAddress)
    {
        return assignDistricts(geocodedAddress, null, DistrictType.getStandardTypes(), SINGLE_DISTRICT_STRATEGY);
    }

    /**
     * Assign standard districts using specified provider
     * @param geocodedAddress
     * @param distProvider
     * @return DistrictResult
     */
    public DistrictResult assignDistricts(final GeocodedAddress geocodedAddress, final String distProvider)
    {
        return assignDistricts(geocodedAddress, distProvider, DistrictType.getStandardTypes(), SINGLE_DISTRICT_STRATEGY);
    }

    /**
     * If a district provider is specified use that for district assignment.
     * Otherwise the default strategy for district assignment is to run both street file and district shape file
     * look-ups in parallel. Once results from both lookup methods are retrieved they are compared and consolidated.
     *
     * @param geocodedAddress
     * @param distProvider
     * @return
     */
    public DistrictResult assignDistricts(final GeocodedAddress geocodedAddress, final String distProvider,
                                          final List<DistrictType> districtTypes, DistrictStrategy districtStrategy)
    {
        logger.debug("Assigning districts " + ((geocodedAddress != null) ? geocodedAddress.getAddress() : ""));
        Timestamp startTime = TimeUtil.currentTimestamp();
        DistrictResult districtResult = null, streetFileResult, shapeFileResult;
        ExecutorService districtExecutor = null;

        if (this.isRegistered(distProvider)) {
            DistrictService districtService = this.getInstance(distProvider);
            districtResult = districtService.assignDistricts(geocodedAddress, districtTypes);
        }
        else {
            try {
                DistrictService shapeFileService = this.getInstance("shapefile");
                DistrictService streetFileService = this.getInstance("streetfile");
                if (districtStrategy == null) {
                    districtStrategy = SINGLE_DISTRICT_STRATEGY;
                }
                logger.debug("Using district assign strategy: " + districtStrategy);

                switch (districtStrategy) {
                    case neighborMatch:
                        districtExecutor = Executors.newFixedThreadPool(2, new SageThreadFactory("neighborMatch"));

                        Callable<DistrictResult> shapeFileCall = getDistrictsCallable(geocodedAddress, shapeFileService, districtTypes);
                        Callable<DistrictResult> streetFileCall = getDistrictsCallable(geocodedAddress, streetFileService, districtTypes);

                        Future<DistrictResult> shapeFileFuture = districtExecutor.submit(shapeFileCall);
                        Future<DistrictResult> streetFileFuture = districtExecutor.submit(streetFileCall);

                        shapeFileResult = shapeFileFuture.get();
                        districtResult = assignNeighbors(shapeFileService, shapeFileResult);

                        streetFileResult = streetFileFuture.get();
                        districtResult = consolidateDistrictResults(geocodedAddress, shapeFileService, shapeFileResult,
                                streetFileResult, DistrictStrategy.neighborMatch);
                        break;

                    case streetFallback:
                        districtExecutor = Executors.newFixedThreadPool(2, new SageThreadFactory("streetFallback"));

                        shapeFileCall = getDistrictsCallable(geocodedAddress, shapeFileService, districtTypes);
                        streetFileCall = getDistrictsCallable(geocodedAddress, streetFileService, districtTypes);

                        shapeFileFuture = districtExecutor.submit(shapeFileCall);
                        streetFileFuture = districtExecutor.submit(streetFileCall);

                        shapeFileResult = shapeFileFuture.get();
                        streetFileResult = streetFileFuture.get();

                        districtResult = consolidateDistrictResults(geocodedAddress, shapeFileService, shapeFileResult,
                                streetFileResult, DistrictStrategy.streetFallback);
                        break;

                    case shapeFallback:
                        streetFileResult = streetFileService.assignDistricts(geocodedAddress, districtTypes);
                        districtResult = consolidateDistrictResults(geocodedAddress, shapeFileService, null, streetFileResult,
                                DistrictStrategy.shapeFallback);
                        break;

                    case streetOnly:
                        districtResult = streetFileService.assignDistricts(geocodedAddress, districtTypes);
                        break;

                    case shapeOnly:
                        districtResult = shapeFileService.assignDistricts(geocodedAddress, districtTypes);
                        break;

                    default:
                        logger.error("Incorrect batch district assignment strategy set. Cannot proceed with assignment!");
                        break;
                }
            }
            catch (InterruptedException ex) {
                logger.error("Failed to get district results from future!", ex);
            }
            catch (ExecutionException ex) {
                logger.error("Failed to get district results from future!", ex);
            }
            finally {
                if (districtExecutor != null) {
                    districtExecutor.shutdownNow();
                }
            }
        }

        districtResult.setSource(DistrictServiceProvider.class);
        districtResult.setGeocodedAddress(geocodedAddress);
        districtResult.setResultTime(new Timestamp(new Date().getTime()));

        if (districtResult.isSuccess()) {
            logger.info(String.format("District assigned in %d ms.", TimeUtil.getElapsedMs(startTime)));
        }
        else {
            logger.warn("Failed to district assign!");
        }
        if (districtResult.getGeocodedAddress() != null) {
            logger.info(FormatUtil.toJsonString(districtResult.getGeocodedAddress()));
        }
        else {
            logger.info("The geocoded address was null");
        }
        return districtResult;
    }

    /** Batch District Assign ----------------------------------------------------------------------------------------*/

    /**
     * Assign standard districts with options set in BatchDistrictRequest.
     * @param bdr
     * @return
     */
    public List<DistrictResult> assignDistricts(final BatchDistrictRequest bdr) {
        return assignDistricts(bdr.getGeocodedAddresses(), bdr.getProvider(), DistrictType.getStandardTypes(), bdr.getDistrictStrategy());
    }

    /**
     * Assign standard districts.
     * @param geocodedAddresses
     * @return
     */
    public List<DistrictResult> assignDistricts(final List<GeocodedAddress> geocodedAddresses) {
        return assignDistricts(geocodedAddresses, DistrictType.getStandardTypes());
    }

    /**
     * Assign specified district types using default method.
     * @param geocodedAddresses
     * @param districtTypes
     * @return List<DistrictResult>
     */
    public List<DistrictResult> assignDistricts(final List<GeocodedAddress> geocodedAddresses,
                                                final List<DistrictType> districtTypes) {
        return assignDistricts(geocodedAddresses, null, districtTypes, null);
    }

    /**
     * Assign specified district types using an assortment of district strategies.
     * @param geocodedAddresses
     * @param distProvider  If district provider is specified, (e.g streetfile), then only that provider will be used.
     * @param districtTypes
     * @return List<DistrictResult>
     */
    public List<DistrictResult> assignDistricts(final List<GeocodedAddress> geocodedAddresses, final String distProvider,
                                                final List<DistrictType> districtTypes, DistrictStrategy districtStrategy)
    {
        if (geocodedAddresses != null) {
            logger.info(String.format("Performing district assign for %d addresses.", geocodedAddresses.size()));
        }
        else {
            logger.info("The geocoded address was null");
        }

        long districtElapsedMs = 0;
        Timestamp startTime = TimeUtil.currentTimestamp();

        ExecutorService districtExecutor = null;
        List<DistrictResult> districtResults = new ArrayList<>(), streetFileResults, shapeFileResults;

        DistrictService streetFileService = this.getInstance("streetfile");
        DistrictService shapeFileService = this.getInstance("shapefile");

        if (this.isRegistered(distProvider)) {
            DistrictService districtService = this.getInstance(distProvider);
            districtResults = districtService.assignDistricts(geocodedAddresses, districtTypes);
        }
        else {
            if (districtStrategy == null) {
                districtStrategy = BATCH_DISTRICT_STRATEGY;
            }
            logger.debug(String.format("Using district assign strategy: %s", districtStrategy));

            try {
                switch (districtStrategy) {
                    case neighborMatch:
                        districtExecutor = Executors.newFixedThreadPool(2, new SageThreadFactory("neighborMatchBatch"));

                        Callable<List<DistrictResult>> streetFileCall = getDistrictsCallable(geocodedAddresses, streetFileService, districtTypes);
                        Callable<List<DistrictResult>> shapeFileCall = getDistrictsCallable(geocodedAddresses, shapeFileService, districtTypes);

                        Future<List<DistrictResult>> shapeFileFuture = districtExecutor.submit(shapeFileCall);
                        Future<List<DistrictResult>> streetFileFuture = districtExecutor.submit(streetFileCall);

                        shapeFileResults = shapeFileFuture.get();
                        streetFileResults = streetFileFuture.get();

                        for (int i = 0; i < shapeFileResults.size(); i++) {
                            assignNeighbors(shapeFileService, shapeFileResults.get(i));
                            DistrictResult consolidated =
                                    consolidateDistrictResults(geocodedAddresses.get(i), shapeFileService, shapeFileResults.get(i),
                                            streetFileResults.get(i), DistrictStrategy.neighborMatch);
                            consolidated.setGeocodedAddress(geocodedAddresses.get(i));
                            if (consolidated.getGeocodedAddress() != null) {
                                logger.info(FormatUtil.toJsonString(consolidated.getGeocodedAddress()));
                            }
                            else {
                                logger.info("The geocoded address was null");
                            }
                            districtResults.add(consolidated);
                        }
                        break;

                    case streetFallback:
                        districtExecutor = Executors.newFixedThreadPool(2, new SageThreadFactory("streetFallbackBatch"));

                        streetFileCall = getDistrictsCallable(geocodedAddresses, streetFileService, districtTypes);
                        shapeFileCall = getDistrictsCallable(geocodedAddresses, shapeFileService, districtTypes);

                        shapeFileFuture = districtExecutor.submit(shapeFileCall);
                        streetFileFuture = districtExecutor.submit(streetFileCall);

                        shapeFileResults = shapeFileFuture.get();
                        streetFileResults = streetFileFuture.get();

                        for (int i = 0; i < shapeFileResults.size(); i++) {
                            DistrictResult consolidated =
                                    consolidateDistrictResults(geocodedAddresses.get(i), shapeFileService, shapeFileResults.get(i),
                                            streetFileResults.get(i), DistrictStrategy.streetFallback);
                            consolidated.setGeocodedAddress(geocodedAddresses.get(i));
                            if (consolidated.getGeocodedAddress() != null) {
                                logger.info(FormatUtil.toJsonString(consolidated.getGeocodedAddress()));
                            }
                            else {
                                logger.info("The geocoded address was null");
                            }
                            districtResults.add(consolidated);
                        }
                        break;

                    case shapeFallback:
                        streetFileResults = streetFileService.assignDistricts(geocodedAddresses, districtTypes);
                        for (int i = 0; i < streetFileResults.size(); i++) {
                            DistrictResult consolidated =
                                    consolidateDistrictResults(geocodedAddresses.get(i), shapeFileService, null, streetFileResults.get(i),
                                            DistrictStrategy.shapeFallback);
                            consolidated.setGeocodedAddress(geocodedAddresses.get(i));
                            if (consolidated.getGeocodedAddress() != null) {
                                logger.info(FormatUtil.toJsonString(consolidated.getGeocodedAddress()));
                            }
                            else {
                                logger.info("The geocoded address was null");
                            }
                            districtResults.add(consolidated);
                        }
                        break;

                    case streetOnly:
                        districtResults = streetFileService.assignDistricts(geocodedAddresses, districtTypes);
                        break;

                    case shapeOnly:
                        districtResults = shapeFileService.assignDistricts(geocodedAddresses, districtTypes);
                        break;

                    default:
                        logger.error("Incorrect batch district assignment strategy set. Cannot proceed with assignment!");
                        break;
                }
            }
            catch (InterruptedException ex) {
                logger.error("Failed to get district results from future!", ex);
            }
            catch (ExecutionException ex) {
                logger.error("Failed to get district results from future!", ex);
            }
            finally {
                if (districtExecutor != null) {
                    districtExecutor.shutdownNow();
                }
            }
        }

        districtElapsedMs = TimeUtil.getElapsedMs(startTime);
        logger.info(String.format("District assign time: %d ms.", districtElapsedMs));

        return districtResults;
    }

    /** Multi District Overlap ---------------------------------------------------------------------------------------*/

    public DistrictResult assignMultiMatchDistricts(GeocodedAddress geocodedAddress, Boolean zipProvided)
    {
        Timestamp startTime = TimeUtil.currentTimestamp();
        DistrictShapefile districtShapeFile = (DistrictShapefile) this.getInstance("shapefile");
        DistrictResult districtResult = districtShapeFile.getMultiMatchResult(geocodedAddress, zipProvided);
        districtResult.setResultTime(new Timestamp(new Date().getTime()));
        logger.info(String.format("Multi-match district assign in %d ms.", TimeUtil.getElapsedMs(startTime)));
        return districtResult;
    }

    /** Callables ----------------------------------------------------------------------------------------------------*/

    private Callable<DistrictResult> getDistrictsCallable(final GeocodedAddress geocodedAddress,
                                                          final DistrictService districtService,
                                                          final List<DistrictType> districtTypes) {
        return new Callable<DistrictResult>() {
            @Override
            public DistrictResult call() throws Exception {
                return districtService.assignDistricts(geocodedAddress, districtTypes);
            }
        };
    }

    private Callable<List<DistrictResult>> getDistrictsCallable(final List<GeocodedAddress> geocodedAddresses,
                                                                final DistrictService districtService,
                                                                final List<DistrictType> districtTypes) {
        return new Callable<List<DistrictResult>>() {
            @Override
            public List<DistrictResult> call() throws Exception {
                return districtService.assignDistricts(geocodedAddresses, districtTypes);
            }
        };
    }

    /** Internal | District Result Consolidation ------------------------------------------------------------------*/

    /**
     * Perform result consolidation based on the specified strategy.
     * @return  Consolidated district result
     */
    private DistrictResult consolidateDistrictResults(GeocodedAddress geocodedAddress, DistrictService shapeService,
                                                      DistrictResult shapeResult, DistrictResult streetResult,
                                                      DistrictStrategy strategy)
    {
        switch (strategy) {
            case neighborMatch:
                if (shapeResult.isSuccess()) {
                    DistrictInfo shapeInfo = shapeResult.getDistrictInfo();
                    String address = (shapeResult.getAddress() != null) ? shapeResult.getAddress().toString() : "Missing Address!";

                    /** Can only consolidate if street result exists */
                    if (streetResult.isSuccess()) {
                        DistrictInfo streetInfo = streetResult.getDistrictInfo();
                        Set<DistrictType> fallbackSet = new HashSet<>(streetResult.getAssignedDistricts());

                        for (DistrictType assignedType : shapeInfo.getAssignedDistricts()) {
                            String shapeCode = shapeInfo.getDistCode(assignedType);
                            String streetCode = streetInfo.getDistCode(assignedType);

                            /** If the street file set assigned the district */
                            if (fallbackSet.contains(assignedType)) {
                                /** If the street/shape files don't agree */
                                if (!shapeCode.equalsIgnoreCase(streetCode)) {

                                    /** Use neighbor matching only on the allowed districts (for performance) */
                                    if (allowNeighborAssignSet.contains(assignedType)) {
                                        /** Check the neighbor districts to see if one of them is the district found in the street result */
                                        List<DistrictMap> neighborMaps = shapeInfo.getNeighborMaps(assignedType);
                                        DistrictMap neighborMap = getNeighborMapByCode(neighborMaps, streetCode);

                                        /** If there is a match in the neighboring districts, set the neighbor district as the actual one */
                                        if (neighborMap != null) {
                                            logger.debug("Consolidating " + assignedType + " district from " + shapeCode + " to " + streetCode + " for " + address);

                                            /** Preserve the original result as it will become the neighbor district */
                                            DistrictMap original = new DistrictMap();
                                            original.setDistrictType(assignedType);
                                            original.setDistrictCode(shapeInfo.getDistCode(assignedType));
                                            original.setDistrictName(shapeInfo.getDistName(assignedType));

                                            /** Apply the new info */
                                            shapeInfo.setDistCode(assignedType, neighborMap.getDistrictCode());
                                            shapeInfo.setDistName(assignedType, neighborMap.getDistrictName());

                                            /** Replace the neighbor */
                                            int index = shapeInfo.getNeighborMaps(assignedType).indexOf(neighborMap);
                                            shapeInfo.getNeighborMaps(assignedType).remove(index);
                                            if (shapeInfo.getNeighborMaps(assignedType).isEmpty()) {
                                                shapeInfo.getNeighborMaps(assignedType).add(original);
                                            }
                                            else {
                                                shapeInfo.getNeighborMaps(assignedType).set(0, original);
                                            }
                                        }
                                        /** Otherwise there was a mismatch between the street and shape files that should be investigated.
                                         *  TODO: Consider logging this better to allow for street file data corrections. */
                                        else {
                                            logger.warn("Shapefile/Streetfile mismatch for district " + assignedType + " for address " + address);
                                        }
                                    }
                                    /** Since neighbor matching is not allowed for this district, just assume street file is correct */
                                    else {
                                        /** Apply the street file data */
                                        logger.debug("Replacing " + assignedType + " district from " + shapeCode + " to " + streetCode + " for " + address);
                                        replaceShapeWithStreet(assignedType, shapeInfo, streetInfo);
                                    }
                                }
                            }
                            /** Otherwise no comparison available. */
                            else {
                                logger.trace(assignedType + " district could not be verified for " + address);
                            }
                        }

                        /** Incorporate all the districts found in street file result that are missing in the shape file result */
                        fallbackSet.removeAll(shapeInfo.getAssignedDistricts());
                        for (DistrictType districtType : fallbackSet) {
                            replaceShapeWithStreet(districtType, shapeInfo, streetInfo);
                        }
                    }
                    else {
                        logger.debug("No street file result for " + address);
                    }
                }
                else {
                    shapeResult = streetResult;
                }
                return shapeResult;

            case streetFallback:
                if (shapeResult.isSuccess()) {
                    DistrictInfo shapeInfo = shapeResult.getDistrictInfo();

                    /** Can only consolidate if street result exists */
                    if (streetResult.isSuccess()) {
                        Set<DistrictType> streetAssignedSet = new HashSet<>(streetResult.getAssignedDistricts());
                        DistrictInfo streetInfo = streetResult.getDistrictInfo();

                        /** Check all street assigned districts */
                        for (DistrictType assignedType : streetAssignedSet) {
                            String streetCode = streetInfo.getDistCode(assignedType);

                            /** If the district is missing from shape or conflicts with street */
                            if (!shapeInfo.getAssignedDistricts().contains(assignedType) ||
                                !shapeInfo.getDistCode(assignedType).equalsIgnoreCase(streetCode)) {

                                /** Apply the street file data */
                                replaceShapeWithStreet(assignedType, shapeInfo, streetInfo);
                                shapeInfo.setDistMap(assignedType, null);
                            }
                        }
                    }
                    return shapeResult;
                }
                else {
                    return streetResult;
                }

            case shapeFallback:
                if (!streetResult.isSuccess()) {
                    return shapeService.assignDistricts(geocodedAddress);
                }
                else {
                    return streetResult;
                }

            case streetOnly:
                return streetResult;

            default:
                logger.error("Cannot consolidate due to invalid district strategy");
        }
        return shapeResult;
    }

    /**
     * Helper method to replace a shape assigned district with a streetfile assigned district.
     * @param districtType  District type to replace
     * @param shapeInfo The shapefile DistrictInfo
     * @param streetInfo The streetfile DistrictInfo
     * @return The shapefile DistrictInfo with modified district code and name. DistrictMap is not altered.
     */
    private DistrictInfo replaceShapeWithStreet(DistrictType districtType, DistrictInfo shapeInfo, DistrictInfo streetInfo)
    {
        shapeInfo.setDistCode(districtType, streetInfo.getDistCode(districtType));
        shapeInfo.setDistName(districtType, streetInfo.getDistName(districtType));
        return shapeInfo;
    }

    /**
     * Assign nearest neighbor districts for any district that meets the proximity condition.
     * @param shapeService  Reference to a shape file lookup instance
     * @param shapeResult   The shape file DistrictResult
     * @return DistrictResult with neighbors set
     */
    private DistrictResult assignNeighbors(DistrictService shapeService, DistrictResult shapeResult)
    {
        if (shapeResult != null && (shapeResult.isSuccess())) {
            DistrictInfo shapeInfo = shapeResult.getDistrictInfo();
            GeocodedAddress geocodedAddress = shapeResult.getGeocodedAddress();

            /** Iterate over all assigned, near border districts */
            for (DistrictType districtType : shapeResult.getAssignedDistricts()) {
                if (shapeInfo.getDistProximity(districtType) < PROXIMITY_THRESHOLD) {

                    /** Designate that the district lines are close */
                    shapeInfo.addNearBorderDistrict(districtType);

                    /** Fetch the neighbors and add them to the district info if it is allowed. */
                    if (allowNeighborAssignSet.contains(districtType)) {
                        Map<String, DistrictMap> neighborDistricts = shapeService.nearbyDistricts(geocodedAddress, districtType);
                        if (neighborDistricts != null && neighborDistricts.size() > 0) {
                            List<DistrictMap> neighborList = new ArrayList<>();
                            for (DistrictMap neighborMap : neighborDistricts.values()) {
                                logger.trace("Adding " + districtType + " neighbor: " + neighborMap.getDistrictCode());
                                neighborList.add(neighborMap);
                            }
                            shapeInfo.addNeighborMaps(districtType, neighborList);
                        }
                    }
                }
            }
        }
        return shapeResult;
    }

    /**
     * Searches through a list of neighbor district maps and returns the neighbor that matches the specified code.
     * @param neighborMaps  List of neighboring DistrictMaps
     * @param code          District code to search for.
     * @return              DistrictMap of the matched neighbor.
     */
    private DistrictMap getNeighborMapByCode(List<DistrictMap> neighborMaps, String code) {
        for (DistrictMap neighborMap : neighborMaps) {
            if (neighborMap.getDistrictCode().equalsIgnoreCase(code)) {
                return neighborMap;
            }
        }
        return null;
    }
}