package gov.nysenate.sage.service.district;

import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.factory.SageThreadFactory;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.provider.district.DistrictService;
import gov.nysenate.sage.util.ExecutorUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Parallel district assignment for use in a provider's batch district implementation.
 */
@Service
public class ParallelDistrictService
{
    private static Logger logger = LoggerFactory.getLogger(ParallelDistrictService.class);
    private int THREAD_COUNT;
    private static ThreadPoolTaskExecutor executor;
    private Environment env;

    @Autowired
    public ParallelDistrictService(Environment env) {
        this.env = env;
        this.THREAD_COUNT = this.env.getValidateThreads();
        this.executor = ExecutorUtil.createExecutor("district", THREAD_COUNT);
//        this.executor = Executors.newFixedThreadPool(THREAD_COUNT, new SageThreadFactory("district"));
    }

    public List<DistrictResult> assignDistricts(DistrictService districtService, List<GeocodedAddress> geocodedAddresses, List<DistrictType> types)
    {
        ArrayList<DistrictResult> districtResults = new ArrayList<>();
        ArrayList<Future<DistrictResult>> futureDistrictResults = new ArrayList<>();

        logger.trace("District Assigning using " + THREAD_COUNT + " threads.");
        for (GeocodedAddress geocodedAddress : geocodedAddresses) {
            futureDistrictResults.add(executor.submit(new ParallelDistAssign(districtService, geocodedAddress, types)));
        }

        for (Future<DistrictResult> districtResult : futureDistrictResults) {
            try {
                districtResults.add(districtResult.get());
            }
            catch (InterruptedException ex) {
                logger.error(ex.getMessage());
            }
            catch (ExecutionException ex) {
                logger.error(ex.getMessage());
            }
        }
        return districtResults;
    }

    public void shutdownThread() {
        executor.shutdown();
    }

    private static class ParallelDistAssign implements Callable<DistrictResult>
    {
        public final DistrictService districtService;
        public final GeocodedAddress geocodedAddress;
        public final List<DistrictType> types;

        public ParallelDistAssign(DistrictService districtService, GeocodedAddress geocodedAddress, List<DistrictType> types)
        {
            this.districtService = districtService;
            this.geocodedAddress = geocodedAddress;
            this.types = types;
        }

        @Override
        public DistrictResult call()
        {
            return districtService.assignDistrictsForBatch(geocodedAddress, types);
        }
    }
}
