package gov.nysenate.sage.provider.district;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Table;
import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.controller.api.DistrictUtil;
import gov.nysenate.sage.dao.provider.district.SqlShapefileDao;
import gov.nysenate.sage.dao.provider.streetfile.StreetfileDao;
import gov.nysenate.sage.model.PostOfficeData;
import gov.nysenate.sage.model.address.*;
import gov.nysenate.sage.model.district.DistrictMatchLevel;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.DistrictResult;
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
    private final Table<Zip5, List<LocalSource>, PostOfficeData<DistrictResult>> poBoxCache = HashBasedTable.create();

    public DistrictService(StreetfileDao streetfileDao, SqlShapefileDao sqlShapefileDao,
                           @Value("${district.ranking}") String districtRankingStr, Environment env) {
        this.streetfileDao = streetfileDao;
        this.sqlShapefileDao = sqlShapefileDao;

        List<LocalSource> tempRanking = new ArrayList<>();
        for (String geocoder : districtRankingStr.split(", *")) {
            tempRanking.add(LocalSource.valueOf(geocoder.toUpperCase()));
        }
        this.defaultRanking = ImmutableList.copyOf(tempRanking);
        this.executor = ExecutorUtil.createExecutor("district", env.getValidateThreads());
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
            if (address instanceof PostOfficeBox) {
                if (address.getZip5() == null) {
                    return new DistrictResult(null, geocodedAddress);
                }
                var cacheResult = poBoxCache.get(address.getZip5(), providers);
                if (cacheResult == null) {
                    final List<LocalSource> finalProviders = providers;
                    List<DistrictResult> postOfficeResults = ((GeocodedPostOfficeBox) geocodedAddress).getPostOffices().stream()
                            .map(geoAddr -> assignDistricts(finalProviders, geoAddr, requiredTypes)).toList();
                    cacheResult = PostOfficeData.getDistrictData(postOfficeResults);
                    poBoxCache.put(address.getZip5(), providers, cacheResult);
                }
                return cacheResult.getData(address.getPostalCity());
            }
        }

        var results = new ArrayList<DistrictResult>();
        for (LocalSource provider : providers) {
            var result = new DistrictResult(provider, geocodedAddress);
            if (result.isSuccess()) {
                if (provider == STREETFILE) {
                    result.setDistrictInfo(streetfileDao.getDistrictInfo((BuildingAddress) address, DistrictMatchLevel.HOUSE));
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

    @PreDestroy
    private void shutdownThreads() {
        executor.shutdown();
    }
}
