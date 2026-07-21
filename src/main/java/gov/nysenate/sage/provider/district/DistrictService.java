package gov.nysenate.sage.provider.district;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import gov.nysenate.sage.util.DistrictUtil;
import gov.nysenate.sage.dao.provider.streetfile.StreetfileDao;
import gov.nysenate.sage.model.PostOfficeCache;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.address.GeocodedPostOfficeBox;
import gov.nysenate.sage.model.address.PostOfficeBox;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.provider.PostOfficeCacheManager;
import gov.nysenate.sage.util.ExecutorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static gov.nysenate.sage.model.Accuracy.HOUSE;
import static gov.nysenate.sage.model.result.ResultStatus.*;

/**
 * Provides methods for mapping GeocodedAddress to DistrictResult.
 */
@Service
public class DistrictService {
    private static final Logger logger = LoggerFactory.getLogger(DistrictService.class);
    private final StreetfileDao streetfileDao;
    private final ShapefileService shapefileService;
    private final ImmutableList<LocalSource> defaultRanking;
    private final ThreadPoolTaskExecutor executor;
    private final PostOfficeCache<LocalSource, DistrictResult> poBoxCache = PostOfficeCacheManager.getDistrictCache();

    public DistrictService(StreetfileDao streetfileDao, ShapefileService shapefileService,
                           @Value("${district.ranking}") String districtRankingStr,
                           @Value("${num.threads:3}") int numThreads) {
        this.streetfileDao = streetfileDao;
        this.shapefileService = shapefileService;

        List<LocalSource> tempRanking = new ArrayList<>();
        for (String districtSource : districtRankingStr.split(", *")) {
            tempRanking.add(LocalSource.valueOf(districtSource.toUpperCase()));
        }
        this.defaultRanking = ImmutableList.copyOf(tempRanking);
        this.executor = ExecutorUtil.createExecutor("district", numThreads);
    }

    public DistrictResult assignDistricts(List<LocalSource> providers, GeocodedAddress geocodedAddress,
                                          Set<DistrictType> requiredTypes) {
        if (providers == null) {
            providers = defaultRanking;
        }

        Address address = geocodedAddress.getAddress();
        if (address.isOutOfState()) {
            return new DistrictResult(NON_NY_STATE);
        }
        if (address instanceof PostOfficeBox poBox) {
            if (geocodedAddress instanceof GeocodedPostOfficeBox poBoxGeoAddr) {
                return getPostOfficeResult(poBox, providers, poBoxGeoAddr);
            }
            else {
                logger.warn("Error handling PO box {}", poBox);
                return new DistrictResult(INTERNAL_ERROR);
            }
        }

        var results = new ArrayList<DistrictResult>();
        // Used to avoid unnecessary geometry queries.
        Set<DistrictType> typesToAssign = EnumSet.copyOf(requiredTypes);
        for (LocalSource provider : providers) {
            ResultStatus status = getStatus(geocodedAddress, provider);
            DistrictInfo districtInfo = DistrictInfo.empty;
            if (status == SUCCESS) {
                districtInfo = switch (provider) {
                    case STREETFILE -> streetfileDao.getDistrictInfo(address);
                    case SHAPEFILE -> shapefileService.getDistrictInfo(geocodedAddress.getGeocode(), typesToAssign);
                };
                typesToAssign.removeAll(districtInfo.getAssignedTypes());
            }
            results.add(new DistrictResult(List.of(provider), status, districtInfo));
        }
        return DistrictUtil.consolidateResults(results);
    }

    public List<DistrictResult> assignDistricts(List<GeocodedAddress> geocodedAddresses,
                                                Set<DistrictType> requiredTypes) {
        var districtResults = new ArrayList<DistrictResult>();
        var futureDistrictResults = new ArrayList<Future<DistrictResult>>();

        for (GeocodedAddress geocodedAddress : geocodedAddresses) {
            futureDistrictResults.add(executor.submit(() -> assignDistricts(null, geocodedAddress, requiredTypes)));
        }

        for (Future<DistrictResult> districtResult : futureDistrictResults) {
            try {
                districtResults.add(districtResult.get());
            }
            catch (InterruptedException | ExecutionException ex) {
                logger.error(ex.getMessage());
                districtResults.add(new DistrictResult(INTERNAL_ERROR));
            }
        }
        return districtResults;
    }

    private DistrictResult getPostOfficeResult(PostOfficeBox poBox, List<LocalSource> providers,
                                                            GeocodedPostOfficeBox geoPoBox) {
        // This strange monitor ensures we won't district assign the same post office in parallel,
        // while allowing other post offices to be assigned at the same time.
        synchronized (String.valueOf(poBox.getZip5()).intern()) {
            var cacheResult = poBoxCache.get(poBox, providers);
            if (cacheResult == null) {
                Multimap<String, DistrictResult> postalCityMap = ArrayListMultimap.create();
                for (GeocodedAddress geoPostOffice : geoPoBox.getPostOffices()) {
                    // Might as well assign all the types
                    postalCityMap.put(geoPostOffice.getAddress().getPostalCity(),
                            assignDistricts(providers, geoPostOffice, Set.of(DistrictType.values())));
                }
                cacheResult = poBoxCache.putAndGet(poBox, providers, postalCityMap);
            }
            return cacheResult;
        }
    }

    @PreDestroy
    private void shutdownThreads() {
        executor.shutdown();
    }

    private static ResultStatus getStatus(final GeocodedAddress geoAddress, LocalSource source) {
        if (geoAddress == null) {
            return MISSING_GEOCODED_ADDRESS;
        }
        if (!geoAddress.isValidAddress() && source == LocalSource.STREETFILE) {
            return INVALID_ADDRESS;
        }
        else if (source == LocalSource.SHAPEFILE) {
            if (!geoAddress.isValidGeocode()) {
                return INVALID_GEOCODE;
            }
            if (geoAddress.getGeocode().accuracy() != HOUSE) {
                return INSUFFICIENT_GEOCODE;
            }
        }
        else if (geoAddress.isValidAddress() && geoAddress.getAddress().isOutOfState()) {
            return NON_NY_STATE;
        }
        return SUCCESS;
    }
}
