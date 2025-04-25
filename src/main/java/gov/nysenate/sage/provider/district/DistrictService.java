package gov.nysenate.sage.provider.district;

import com.google.common.collect.ImmutableList;
import gov.nysenate.sage.controller.api.DistrictUtil;
import gov.nysenate.sage.dao.provider.district.SqlShapefileDao;
import gov.nysenate.sage.dao.provider.streetfile.StreetfileDao;
import gov.nysenate.sage.model.PostOfficeCache;
import gov.nysenate.sage.model.address.*;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.provider.PostOfficeCacheManager;
import gov.nysenate.sage.util.ExecutorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static gov.nysenate.sage.provider.district.LocalSource.SHAPEFILE;
import static gov.nysenate.sage.provider.district.LocalSource.STREETFILE;

/**
 * Provides methods for mapping GeocodedAddress to DistrictResult.
 */
@Service
public class DistrictService {
    private static final Logger logger = LoggerFactory.getLogger(DistrictService.class);
    private final StreetfileDao streetfileDao;
    private final SqlShapefileDao sqlShapefileDao;
    private final ImmutableList<LocalSource> defaultRanking;
    private final ThreadPoolTaskExecutor executor;
    private final PostOfficeCache<LocalSource, DistrictResult> poBoxCache = PostOfficeCacheManager.getDistrictCache();

    public DistrictService(StreetfileDao streetfileDao, SqlShapefileDao sqlShapefileDao,
                           @Value("${district.ranking}") String districtRankingStr,
                           @Value("${num.threads:3}") int numThreads) {
        this.streetfileDao = streetfileDao;
        this.sqlShapefileDao = sqlShapefileDao;

        List<LocalSource> tempRanking = new ArrayList<>();
        for (String geocoder : districtRankingStr.split(", *")) {
            tempRanking.add(LocalSource.valueOf(geocoder.toUpperCase()));
        }
        this.defaultRanking = ImmutableList.copyOf(tempRanking);
        this.executor = ExecutorUtil.createExecutor("district", numThreads);
    }

    public DistrictResult assignDistricts(List<LocalSource> providers, GeocodedAddress geocodedAddress,
                                          List<DistrictType> requiredTypes) {
        if (providers == null) {
            providers = defaultRanking;
        }

        if (geocodedAddress == null) {
            return new DistrictResult(null, null);
        }
        Address address = geocodedAddress.getAddress();
        if (address != null) {
            if (address.isOutOfState() || !address.isValid()) {
                return new DistrictResult(null, geocodedAddress);
            }
            if (address instanceof PostOfficeBox poBox) {
                if (geocodedAddress instanceof GeocodedPostOfficeBox poBoxGeoAddr) {
                    return getPostOfficeResult(poBox, providers, poBoxGeoAddr, requiredTypes);
                }
                else {
                    logger.warn("Error handling PO box {}", poBox);
                    return new DistrictResult(null, geocodedAddress);
                }
            }
        }

        var results = new ArrayList<DistrictResult>();
        for (LocalSource provider : providers) {
            var result = new DistrictResult(provider, geocodedAddress);
            if (result.isSuccess()) {
                if (provider == STREETFILE) {
                    result.setDistrictInfo(streetfileDao.getDistrictInfo((BuildingAddress) address));
                }
                else if (provider == SHAPEFILE) {
                    result.setDistrictInfo(sqlShapefileDao.getDistrictInfo(geocodedAddress.getGeocode(), requiredTypes));
                }
            }
            result.setResultTime();
            results.add(result);
        }

        return DistrictUtil.consolidateResults(results);
    }

    public List<DistrictResult> assignDistricts(List<GeocodedAddress> geocodedAddresses,
                                                List<DistrictType> requiredTypes) {
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
            }
        }
        return districtResults;
    }

    private synchronized DistrictResult getPostOfficeResult(PostOfficeBox poBox, List<LocalSource> providers,
                                                            GeocodedPostOfficeBox geoPoBox,
                                                            List<DistrictType> requiredTypes) {
        var cacheResult = poBoxCache.get(poBox, providers);
        if (cacheResult == null) {
            List<DistrictResult> postOfficeResults = geoPoBox.getPostOffices().stream()
                    .map(geoAddr -> assignDistricts(providers, geoAddr, requiredTypes)).toList();
            cacheResult = poBoxCache.putAndGet(poBox, providers, postOfficeResults);
        }
        return cacheResult;
    }

    @PreDestroy
    private void shutdownThreads() {
        executor.shutdown();
    }
}
