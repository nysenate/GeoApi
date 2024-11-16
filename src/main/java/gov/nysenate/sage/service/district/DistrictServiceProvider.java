package gov.nysenate.sage.service.district;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.DistrictedAddress;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.api.BatchDistrictRequest;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.provider.district.DistrictService;
import gov.nysenate.sage.provider.district.DistrictShapefile;
import gov.nysenate.sage.provider.district.DistrictSource;
import gov.nysenate.sage.provider.district.Streetfile;
import gov.nysenate.sage.service.PostOfficeService;
import gov.nysenate.sage.util.FormatUtil;
import gov.nysenate.sage.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;

import static gov.nysenate.sage.provider.district.DistrictSource.SHAPEFILE;
import static gov.nysenate.sage.provider.district.DistrictSource.STREETFILE;

/**
 * Point of access for all district assignment requests. This class maintains a collection of available
 * district providers and contains logic for distributing requests and collecting responses from the providers.
 */
@Service
public class DistrictServiceProvider implements SageDistrictServiceProvider {
    public enum DistrictStrategy {
        // TODO: rename, because this uses streetfile values even if shape lookup succeeds
        // Perform shape lookup and consolidate street file results.
        streetFallback,
        // Perform street lookup and only fall back to shape files when street lookup failed.
        shapeFallback,
        // Perform street lookup only
        streetOnly,
        // Perform shape lookup only
        shapeOnly
    }

    private static final Logger logger = LoggerFactory.getLogger(DistrictServiceProvider.class);

    private final DistrictStrategy singleDistrictStrategy;
    private final DistrictStrategy batchDistrictStrategy;

    private final Map<DistrictSource, DistrictService> providers = new HashMap<>();
    private final PostOfficeService postOfficeService;

    @Autowired
    public DistrictServiceProvider(@Value("${district.strategy.single:streetFallback}") String singleDistrictStrategy,
                                   @Value("${district.strategy.batch:streetFallback}") String batchDistrictStrategy,
                                   DistrictShapefile districtShapefile, Streetfile streetFile, PostOfficeService postOfficeService) {
        this.postOfficeService = postOfficeService;
        providers.put(SHAPEFILE, districtShapefile);
        providers.put(STREETFILE, streetFile);
        this.singleDistrictStrategy = DistrictStrategy.valueOf(singleDistrictStrategy);
        this.batchDistrictStrategy = DistrictStrategy.valueOf(batchDistrictStrategy);
    }

    /**
     * If a district provider is specified use that for district assignment.
     * Otherwise, the default strategy for district assignment is to run both street file and district shape file
     * look-ups in parallel. Once results from both lookup methods are retrieved they are compared and consolidated.
     */
    // TODO: should simplify DistrictStrategy vs. District Provider (fallback idea?)
    public DistrictResult assignDistricts(final GeocodedAddress geocodedAddress, final DistrictSource distProvider,
                                          final List<DistrictType> districtTypes, DistrictStrategy districtStrategy) {
        Timestamp startTime = TimeUtil.currentTimestamp();
        DistrictResult streetFileResult = providers.get(STREETFILE).assignDistricts(geocodedAddress, districtTypes);
        DistrictResult shapeFileResult = providers.get(SHAPEFILE).assignDistricts(geocodedAddress, districtTypes);
        DistrictResult districtResult;

        if (providers.containsKey(distProvider)) {
            DistrictService districtService = this.providers.get(distProvider);
            districtResult = districtService.assignDistricts(geocodedAddress, districtTypes);
        }
        else {
            if (districtStrategy == null) {
                districtStrategy = singleDistrictStrategy;
            }
            districtResult = consolidateDistrictResults(shapeFileResult, streetFileResult, districtStrategy);
        }
        fixPostOfficeBoxResult(districtResult);
        districtResult.setResultTime();

        if (districtResult.isSuccess()) {
            logger.info("District assigned in {} ms.", TimeUtil.getElapsedMs(startTime));
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

    /**
     * Assign standard districts with options set in BatchDistrictRequest.
     */
    public List<DistrictResult> assignDistricts(final BatchDistrictRequest bdr) {
        return assignDistricts(bdr.getGeocodedAddresses(), bdr.getProvider(), DistrictType.getStandardTypes(), bdr.getDistrictStrategy());
    }

    /**
     * Assign specified district types using an assortment of district strategies.
     * @param distProvider  If district provider is specified, (e.g streetfile), then only that provider will be used.
     * @return List<DistrictResult>
     */
    public List<DistrictResult> assignDistricts(final List<GeocodedAddress> geocodedAddresses, DistrictSource distProvider,
                                                final List<DistrictType> districtTypes, DistrictStrategy districtStrategy) {
        if (districtStrategy == null) {
            districtStrategy = batchDistrictStrategy;
        }
        Timestamp startTime = TimeUtil.currentTimestamp();
        List<DistrictResult> districtResults = new ArrayList<>();

        if (districtStrategy == DistrictStrategy.streetOnly) {
            distProvider = STREETFILE;
        }
        else if (districtStrategy == DistrictStrategy.shapeOnly) {
            distProvider = SHAPEFILE;
        }
        if (providers.containsKey(distProvider)) {
            DistrictService districtService = providers.get(distProvider);
            districtResults = districtService.assignDistricts(geocodedAddresses, districtTypes);
        }
        else {
            List<DistrictResult> streetFileResults = providers.get(STREETFILE).assignDistricts(geocodedAddresses, districtTypes);
            List<DistrictResult> shapeFileResults = providers.get(SHAPEFILE).assignDistricts(geocodedAddresses, districtTypes);

            if (streetFileResults.size() != shapeFileResults.size()) {
                throw new IllegalStateException("District result sizes must match.");
            }
            for (int i = 0; i < streetFileResults.size(); i++) {
                districtResults.add(consolidateDistrictResults(shapeFileResults.get(i), streetFileResults.get(i), districtStrategy));
            }
        }
        districtResults.forEach(this::fixPostOfficeBoxResult);
        logger.info("District assign time: {} ms.", TimeUtil.getElapsedMs(startTime));

        return districtResults;
    }

    private void fixPostOfficeBoxResult(DistrictResult result) {
        Address currAddr = result.getAddress();
        if (currAddr != null && currAddr.isPOBox()) {
            DistrictedAddress poResult = postOfficeService.getDistrictedAddress(currAddr.getZip5(), currAddr.getPostalCity());
            if (poResult != null) {
                result.setStatusCode(ResultStatus.SUCCESS);
                result.setDistrictedAddress(poResult);
            }
        }
    }

    /** Multi District Overlap ---------------------------------------------------------------------------------------*/

    public DistrictResult assignMultiMatchDistricts(GeocodedAddress geocodedAddress, boolean zipProvided) {
        var districtShapeFile = (DistrictShapefile) providers.get(SHAPEFILE);
        DistrictResult districtResult = districtShapeFile.getMultiMatchResult(geocodedAddress, zipProvided);
        districtResult.setResultTime();
        return districtResult;
    }

    /**
     * Perform result consolidation based on the specified strategy.
     * @return  Consolidated district result
     */
    private DistrictResult consolidateDistrictResults(DistrictResult shapeResult, DistrictResult streetResult, DistrictStrategy strategy) {
        switch (strategy) {
            case shapeOnly -> {
                return shapeResult;
            }
            case shapeFallback -> {
                return streetResult.isSuccess() ? streetResult : shapeResult;
            }
            case streetFallback -> {
                if (!shapeResult.isSuccess()) {
                    return streetResult;
                }
                DistrictInfo shapeInfo = shapeResult.getDistrictInfo();
                Set<DistrictType> streetAssignedSet = new HashSet<>(streetResult.getAssignedDistricts());
                DistrictInfo streetInfo = streetResult.getDistrictInfo();

                // Check all streetfile assigned districts
                for (DistrictType assignedType : streetAssignedSet) {
                    String streetCode = streetInfo.getDistCode(assignedType);

                    // Apply streetfile data on conflicts.
                    if (!shapeInfo.getAssignedDistricts().contains(assignedType) ||
                            !shapeInfo.getDistCode(assignedType).equalsIgnoreCase(streetCode)) {
                        shapeInfo.setDistCode(assignedType, streetInfo.getDistCode(assignedType));
                        shapeInfo.setDistName(assignedType, streetInfo.getDistName(assignedType));
                    }
                }
                return shapeResult;
            }
            default -> {
                return streetResult;
            }
        }
    }
}
