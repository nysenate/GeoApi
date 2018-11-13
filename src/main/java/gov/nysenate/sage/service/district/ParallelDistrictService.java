package gov.nysenate.sage.service.district;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.factory.SageThreadFactory;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.util.Config;
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
public abstract class ParallelDistrictService extends BaseDao
{
    private static Logger logger = LoggerFactory.getLogger(ParallelDistrictService.class);
    private Config config = getConfig();
    private int THREAD_COUNT = Integer.parseInt(config.getValue("distassign.threads", "3"));
    private ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT, new SageThreadFactory("district"));

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
        executor.shutdownNow();
    }
}
