package gov.nysenate.sage.service.district;

import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.geo.Polygon;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.service.base.ServiceProviders;
import gov.nysenate.sage.util.Config;
import gov.nysenate.sage.util.FormatUtil;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.*;

/**
 * Point of access for all district assignment requests. This class maintains a collection of available
 * district providers and contains logic for distributing requests and collecting responses from the providers.
 */
public class DistrictServiceProvider extends ServiceProviders<DistrictService>
{
    private final Logger logger = Logger.getLogger(DistrictServiceProvider.class);
    private Config config = ApplicationFactory.getConfig();
    private final static String DEFAULT_DISTRICT_PROVIDER = "shapefile";
    private final static LinkedList<String> DEFAULT_DISTRICT_FALLBACK = new LinkedList<>(Arrays.asList("streetfile", "geoserver"));

    /** Specifies the distance to a district boundary in which the accuracy of shapefiles is uncertain */
    private static Double PROXIMITY_THRESHOLD = 0.001;

    public DistrictServiceProvider()
    {
        PROXIMITY_THRESHOLD = Double.parseDouble(this.config.getValue("proximity.threshold", "0.001"));
    }

    /**
     * Assign standard districts using default method.
     * @param geocodedAddress
     * @return DistrictResult
     */
    public DistrictResult assignDistricts(final GeocodedAddress geocodedAddress)
    {
        return assignDistricts(geocodedAddress, null, DistrictType.getStandardTypes(), false, false);
    }

    /**
     * Assign standard districts using specified provider
     * @param geocodedAddress
     * @param distProvider
     * @return DistrictResult
     */
    public DistrictResult assignDistricts(final GeocodedAddress geocodedAddress, final String distProvider)
    {
        return assignDistricts(geocodedAddress, distProvider, DistrictType.getStandardTypes(), false, false);
    }

    /**
     * If a district provider is specified use that for district assignment.
     * Otherwise the default strategy for district assignment is to run both street file and district shape file
     * look-ups in parallel. Once results from both lookup methods are retrieved they are compared and consolidated.
     *
     * @param geocodedAddress
     * @param distProvider
     * @param getMembers
     * @param getMaps
     * @return
     */
    public DistrictResult assignDistricts(final GeocodedAddress geocodedAddress, final String distProvider,
                                          final List<DistrictType> districtTypes, final boolean getMembers, final boolean getMaps)
    {
        logger.info("Assigning districts " + ((geocodedAddress != null) ? geocodedAddress.getAddress() : ""));
        DistrictResult districtResult = null;
        ExecutorService districtExecutor = null;

        if (this.isRegistered(distProvider)) {
            DistrictService districtService = this.newInstance(distProvider);
            districtService.fetchMaps(getMaps);
            districtResult = districtService.assignDistricts(geocodedAddress, districtTypes);
            districtResult.setGeocodedAddress(geocodedAddress);
        }
        else {
            try {
                districtExecutor = Executors.newFixedThreadPool(2);

                DistrictService shapeFileService = this.newInstance("shapefile");
                DistrictService streetFileService = this.newInstance("streetfile");

                Callable<DistrictResult> shapeFileCall = getDistrictsCallable(geocodedAddress, shapeFileService, districtTypes, getMaps);
                Callable<DistrictResult> streetFileCall = getDistrictsCallable(geocodedAddress, streetFileService, districtTypes, false);

                Future<DistrictResult> shapeFileFuture = districtExecutor.submit(shapeFileCall);
                Future<DistrictResult> streetFileFuture = districtExecutor.submit(streetFileCall);

                DistrictResult shapeFileResult = shapeFileFuture.get();
                DistrictResult streetFileResult = streetFileFuture.get();

                districtResult = assignNeighbors(shapeFileService, shapeFileResult, getMaps);
                districtResult = consolidateDistrictResults(shapeFileService, shapeFileResult, streetFileResult);

                if (getMembers) {
                    DistrictMemberProvider.assignDistrictMembers(districtResult);
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

        if (getMembers) {
            DistrictMemberProvider.assignDistrictMembers(districtResult);
        }
        return districtResult;
    }

    public List<DistrictResult> assignDistricts(final List<GeocodedAddress> geocodedAddresses,
                                                final List<DistrictType> districtTypes) {
        return assignDistricts(geocodedAddresses, null, districtTypes, false, false);
    }

    public List<DistrictResult> assignDistricts(final List<GeocodedAddress> geocodedAddresses, final String distProvider,
                                                final List<DistrictType> districtTypes, final boolean getMembers, final boolean getMaps)
    {
        ExecutorService districtExecutor;
        List<DistrictResult> districtResults = new ArrayList<>();

        if (this.isRegistered(distProvider)) {
            DistrictService districtService = this.newInstance(distProvider);
            districtService.fetchMaps(getMaps);
            districtResults = districtService.assignDistricts(geocodedAddresses, districtTypes);
        }
        else {
            try {
                districtExecutor = Executors.newFixedThreadPool(2);

                DistrictService streetFileService = this.newInstance("streetfile");
                DistrictService shapeFileService = this.newInstance("shapefile");

                Callable<List<DistrictResult>> streetFileCall = getDistrictsCallable(geocodedAddresses, streetFileService, districtTypes, false);
                Callable<List<DistrictResult>> shapeFileCall = getDistrictsCallable(geocodedAddresses, shapeFileService, districtTypes, getMaps);

                Future<List<DistrictResult>> shapeFileFuture = districtExecutor.submit(shapeFileCall);
                Future<List<DistrictResult>> streetFileFuture = districtExecutor.submit(streetFileCall);

                List<DistrictResult> shapeFileResults = shapeFileFuture.get();
                List<DistrictResult> streetFileResults = streetFileFuture.get();

                for (int i = 0; i < shapeFileResults.size(); i++) {
                    districtResults.add(consolidateDistrictResults(shapeFileService, shapeFileResults.get(i),
                                                                   streetFileResults.get(i)));
                }
            }
            catch (InterruptedException ex) {
                logger.error("Failed to get district results from future!", ex);
            }
            catch (ExecutionException ex) {
                logger.error("Failed to get district results from future!", ex);
            }
        }

        if (getMembers) {
            for (DistrictResult districtResult : districtResults) {
                DistrictMemberProvider.assignDistrictMembers(districtResult);
            }
        }

        return districtResults;
    }

    private Callable<DistrictResult> getDistrictsCallable(final GeocodedAddress geocodedAddress,
                                                          final DistrictService districtService,
                                                          final List<DistrictType> districtTypes, final boolean getMaps) {
        return new Callable<DistrictResult>() {
            @Override
            public DistrictResult call() throws Exception {
                if (districtService.providesMaps()) {
                    districtService.fetchMaps(getMaps);
                }
                return districtService.assignDistricts(geocodedAddress, districtTypes);
            }
        };
    }

    private Callable<List<DistrictResult>> getDistrictsCallable(final List<GeocodedAddress> geocodedAddresses,
                                                                final DistrictService districtService,
                                                                final List<DistrictType> districtTypes, final boolean getMaps) {
        return new Callable<List<DistrictResult>>() {
            @Override
            public List<DistrictResult> call() throws Exception {
                if (districtService.providesMaps()) {
                    districtService.fetchMaps(getMaps);
                }
                return districtService.assignDistricts(geocodedAddresses, districtTypes);
            }
        };
    }

    /**
     * Shapefile lookups return a proximity metric that indicates how close the geocode is to a district boundary.
     * If it is too close to the boundary it is possible that the neighboring district is in fact the valid one.
     * If the streetfile lookup returned a match for that district type then we can check to see if that district
     * is in the subset of neighboring districts returned by the shapefiles. If it is then we declare the neighbor
     * district to be the valid one. If there are districts in the street result that do not exist in the shapefile
     * result, apply them to the consolidated result. In the event of a district mismatch between the two and the
     * shapefile is not near the proximity threshold, log the case and give preference to the shapefile.
     *
     * @param shapeResult   Result obtained from shapefile district assign
     * @param streetResult  Result obtained from streetfile district assign
     * @return  Consolidated district result
     */
    private DistrictResult consolidateDistrictResults(DistrictService shapeService, DistrictResult shapeResult,
                                                      DistrictResult streetResult)
    {
        if (shapeResult.isSuccess() || shapeResult.isPartialSuccess()) {
            /** Retrieve objects from result */
            DistrictInfo shapeInfo = shapeResult.getDistrictInfo();
            String address = (shapeResult.getAddress() != null) ? shapeResult.getAddress().toString() : "Missing Address!";

            /** Can only consolidate if a second set of results exists */
            if (streetResult.isSuccess() || streetResult.isPartialSuccess()) {
                DistrictInfo streetInfo = streetResult.getDistrictInfo();
                Set<DistrictType> fallbackSet = new HashSet<>(streetResult.getAssignedDistricts());

                /** Only worry about instances where the location is close to a boundary */
                for (DistrictType assignedType : shapeInfo.getNearBorderDistricts()) {
                    String shapeCode = shapeInfo.getDistCode(assignedType);
                    String streetCode = streetInfo.getDistCode(assignedType);

                    /** If the street/shape files don't agree */
                    if (fallbackSet.contains(assignedType) && !shapeCode.equalsIgnoreCase(streetCode)) {

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
                            original.setPolygons((shapeInfo.getDistMap(assignedType) != null) ? shapeInfo.getDistMap(assignedType).getPolygons()
                                                                                              : new ArrayList<Polygon>());

                            /** Apply the new info */
                            shapeInfo.setDistCode(assignedType, neighborMap.getDistrictCode());
                            shapeInfo.setDistName(assignedType, neighborMap.getDistrictName());
                            shapeInfo.setDistMap(assignedType, neighborMap);

                            /** Replace the neighbor */
                            int index = shapeInfo.getNeighborMaps(assignedType).indexOf(neighborMap);
                            shapeInfo.getNeighborMaps(assignedType).remove(index);
                            shapeInfo.getNeighborMaps(assignedType).set(0, original);
                        }
                        /** Otherwise there was a mismatch between the street and shape files that can't be corrected */
                        else {
                            logger.warn("Shapefile/Streetfile mismatch for district " + assignedType + " for address " + address);
                        }
                    }
                    /** No comparison available. */
                    else {
                        logger.trace(assignedType + " district could not be verified for " + address);
                    }
                }

                /** Incorporate all the districts found in street file result that are missing in the shape file result */
                fallbackSet.removeAll(shapeInfo.getAssignedDistricts());
                for (DistrictType districtType : fallbackSet) {
                    shapeInfo.setDistCode(districtType, streetInfo.getDistCode(districtType));
                }
            }
            else {
                logger.info("No street file result for " + address);
            }
        }
        else {
            shapeResult = streetResult;
        }
        return shapeResult;
    }

    /**
     *
     * @param shapeService
     * @param shapeResult
     * @param getMaps
     * @return
     */
    private DistrictResult assignNeighbors(DistrictService shapeService, DistrictResult shapeResult, boolean getMaps)
    {
        if (shapeResult != null && (shapeResult.isSuccess() || shapeResult.isPartialSuccess())) {
            DistrictInfo shapeInfo = shapeResult.getDistrictInfo();
            GeocodedAddress geocodedAddress = shapeResult.getGeocodedAddress();

            /** Iterate over all assigned, near border districts */
            for (DistrictType districtType : shapeResult.getAssignedDistricts()) {
                if (shapeInfo.getDistProximity(districtType) < PROXIMITY_THRESHOLD) {
                    /** Designate that the district lines are close */
                    shapeInfo.addNearBorderDistrict(districtType);

                    /** Fetch the neighbors and add them to the district info */
                    Map<String, DistrictMap> neighborDistricts = shapeService.nearbyDistricts(geocodedAddress, districtType);
                    if (neighborDistricts != null && neighborDistricts.size() > 0) {
                        List neighborList = new ArrayList<DistrictMap>();
                        for (DistrictMap neighborMap : neighborDistricts.values()) {
                            /** Clear out polygons if map data is not requested */
                            if (!getMaps) {
                                neighborMap.setPolygons(new ArrayList<Polygon>());
                            }
                            neighborList.add(neighborMap);
                        }
                        shapeInfo.addNeighborMaps(districtType, neighborList);
                    }
                }
            }
        }
        return shapeResult;
    }

    /**
     *
     * @param neighborMaps
     * @param code
     * @return
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