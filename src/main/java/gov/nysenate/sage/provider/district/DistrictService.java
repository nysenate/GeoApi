package gov.nysenate.sage.provider.district;

import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.controller.api.DistrictUtil;
import gov.nysenate.sage.dao.data.PostOfficeDao;
import gov.nysenate.sage.dao.provider.district.SqlDistrictShapefileDao;
import gov.nysenate.sage.dao.provider.streetfile.StreetfileDao;
import gov.nysenate.sage.model.PostOfficeData;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.BuildingAddress;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.address.Zip5;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictMatchLevel;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.model.result.ResultStatus;
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

import static gov.nysenate.sage.provider.district.DistrictSource.*;

/**
 * DistrictService is used to assign district information to addresses and may or may not require
 * geo-coordinate information.
 */
@Service
public class DistrictService {
    private static final Logger logger = LoggerFactory.getLogger(DistrictService.class);
    private final PostOfficeDao postOfficeDao;
    private final Map<Tuple<Zip5, List<DistrictSource>>, PostOfficeData<DistrictResult>> poBoxCache = new HashMap<>();
    private final StreetfileDao streetfileDao;
    private final SqlDistrictShapefileDao shapefileDao;
    private final ThreadPoolTaskExecutor executor;


    public DistrictService(PostOfficeDao postOfficeDao, StreetfileDao streetfileDao,
                           SqlDistrictShapefileDao shapefileDao, Environment env) {
        this.postOfficeDao = postOfficeDao;
        this.streetfileDao = streetfileDao;
        this.shapefileDao = shapefileDao;
        this.executor = ExecutorUtil.createExecutor("district", env.getValidateThreads());
    }

    public DistrictResult assignDistricts(List<DistrictSource> providers, GeocodedAddress geocodedAddress) {
        return assignDistricts(providers, geocodedAddress, List.of(DistrictType.values()));
    }

    public DistrictResult assignDistricts(List<DistrictSource> providers, GeocodedAddress geocodedAddress,
                                          List<DistrictType> requiredTypes) {

        Address address = geocodedAddress.getAddress();
        if (address != null && address.isPoBox()) {
            var zip5 = new Zip5(address.getZip5());
            var cacheKey = new Tuple<>(zip5, providers);
            if (!poBoxCache.containsKey(cacheKey)) {
                List<DistrictResult> postOfficeResults = postOfficeDao.getPostOffices(address.getZip5())
                        .stream().map(addr -> assignDistricts(providers, geocodedAddress, requiredTypes)).toList();
                poBoxCache.put(cacheKey, PostOfficeData.getPostOfficeDistrictData(postOfficeResults));
            }
            return poBoxCache.get(cacheKey).getData(address.getPostalCity());
        }

        ResultStatus currStatusCode = null;
        List<DistrictInfo> validInfos = new ArrayList<>();
        for (DistrictSource provider : providers) {
            var result = new DistrictResult(provider, geocodedAddress);
            if (!result.isSuccess()) {
                if (currStatusCode == null) {
                    currStatusCode = result.getStatusCode();
                }
                // Using this to cover when there are multiple problems TODO new status
                else if (currStatusCode != ResultStatus.SUCCESS) {
                    currStatusCode = ResultStatus.MISSING_GEOCODED_ADDRESS;
                }
            }
            else if (provider == STREETFILE) {
                validInfos.add(streetfileDao.getDistrictInfo((BuildingAddress) address, DistrictMatchLevel.HOUSE));
            }
            else if (provider == SHAPEFILE) {
                validInfos.add(shapefileDao.getDistrictInfo(geocodedAddress.getGeocode().point(), requiredTypes));
            }
        }
        var finalResult = new DistrictResult(providers.size() == 1 ? providers.get(0) : STREETFILE_AND_SHAPEFILE,
                geocodedAddress, currStatusCode);
        finalResult.setDistrictInfo(DistrictUtil.consolidateDistrictInfo(validInfos));
        finalResult.setResultTime();
        return finalResult;
    }

    public List<DistrictResult> assignDistricts(List<DistrictSource> providers,
                                                List<GeocodedAddress> geocodedAddresses) {
        return assignDistricts(providers, geocodedAddresses, List.of(DistrictType.values()));
    }

    public List<DistrictResult> assignDistricts(List<DistrictSource> providers,
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
