package gov.nysenate.sage.service.district;

import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.provider.district.DistrictService;
import gov.nysenate.sage.util.ExecutorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Parallel district assignment for use in a provider's batch district implementation.
 */
@Service
public class ParallelDistrictService implements SageParallelDistrictService {
    private static final Logger logger = LoggerFactory.getLogger(ParallelDistrictService.class);
    private final int thread_count;
    private final ThreadPoolTaskExecutor executor;

    @Autowired
    public ParallelDistrictService(Environment env) {
        this.thread_count = env.getValidateThreads();
        this.executor = ExecutorUtil.createExecutor("district", thread_count);
    }

    public List<DistrictResult> assignDistricts(DistrictService districtService, List<GeocodedAddress> geocodedAddresses, List<DistrictType> types) {
        var districtResults = new ArrayList<DistrictResult>();
        var futureDistrictResults = new ArrayList<Future<DistrictResult>>();

        logger.trace("District Assigning using " + thread_count + " threads.");
        for (GeocodedAddress geocodedAddress : geocodedAddresses) {
            futureDistrictResults.add(executor.submit(new ParallelDistAssign(districtService, geocodedAddress, types)));
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

    public void shutdownThread() {
        executor.shutdown();
    }

    private record ParallelDistAssign(DistrictService districtService, GeocodedAddress geocodedAddress,
                                      List<DistrictType> types) implements Callable<DistrictResult> {
        @Override
            public DistrictResult call() {
                return districtService.assignDistrictsForBatch(geocodedAddress, types);
            }
        }
}
