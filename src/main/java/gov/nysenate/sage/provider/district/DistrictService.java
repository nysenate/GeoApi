package gov.nysenate.sage.provider.district;

import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.controller.api.DistrictUtil;
import gov.nysenate.sage.dao.data.PostOfficeDao;
import gov.nysenate.sage.dao.provider.district.SqlShapefileDao;
import gov.nysenate.sage.dao.provider.streetfile.StreetfileDao;
import gov.nysenate.sage.model.PostOfficeData;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.BuildingAddress;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.address.Zip5;
import gov.nysenate.sage.model.district.DistrictMatchLevel;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.util.ExecutorUtil;
import gov.nysenate.sage.util.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final PostOfficeDao postOfficeDao;
    private final Map<Tuple<Zip5, List<LocalSource>>, PostOfficeData<DistrictResult>> poBoxCache = new HashMap<>();
    private final StreetfileDao streetfileDao;
    private final SqlShapefileDao sqlShapefileDao;
    private final ThreadPoolTaskExecutor executor;

    public DistrictService(PostOfficeDao postOfficeDao, StreetfileDao streetfileDao,
                           SqlShapefileDao sqlShapefileDao, Environment env) {
        this.postOfficeDao = postOfficeDao;
        this.streetfileDao = streetfileDao;
        this.sqlShapefileDao = sqlShapefileDao;
        this.executor = ExecutorUtil.createExecutor("district", env.getValidateThreads());
    }

    public DistrictResult assignDistricts(List<LocalSource> providers, GeocodedAddress geocodedAddress,
                                          List<DistrictType> requiredTypes) {
        Address address = geocodedAddress.getAddress();
        if (address != null && address.isPoBox()) {
            var cacheKey = new Tuple<>(address.getZip5(), providers);
            if (!poBoxCache.containsKey(cacheKey)) {
                List<DistrictResult> postOfficeResults = postOfficeDao.getPostOffices(address.getZip5())
                        .stream().map(addr -> assignDistricts(providers, geocodedAddress, requiredTypes)).toList();
                poBoxCache.put(cacheKey, PostOfficeData.getDistrictData(postOfficeResults));
            }
            return poBoxCache.get(cacheKey).getData(address.getPostalCity());
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

    public List<DistrictResult> assignDistricts(List<LocalSource> providers,
                                                List<GeocodedAddress> geocodedAddresses,
                                                List<DistrictType> requiredTypes) {
        var districtResults = new ArrayList<DistrictResult>();
        var futureDistrictResults = new ArrayList<Future<DistrictResult>>();

        for (GeocodedAddress geocodedAddress : geocodedAddresses) {
            futureDistrictResults.add(executor.submit(() -> assignDistricts(providers, geocodedAddress, requiredTypes)));
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
