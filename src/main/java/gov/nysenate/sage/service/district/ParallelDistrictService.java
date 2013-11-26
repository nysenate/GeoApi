package gov.nysenate.sage.service.district;

import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.factory.SageThreadFactory;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.util.Config;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Parallel district assignment for use in a provider's batch district implementation.
 */
public abstract class ParallelDistrictService
{
    private static Logger logger = Logger.getLogger(ParallelDistrictService.class);
    private static Config config = ApplicationFactory.getConfig();
    private static int THREAD_COUNT = Integer.parseInt(config.getValue("distassign.threads", "3"));
    private static ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT, new SageThreadFactory("district"));

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

    public static List<DistrictResult> assignDistricts(DistrictService districtService, List<GeocodedAddress> geocodedAddresses, List<DistrictType> types)
    {
        ArrayList<DistrictResult> districtResults = new ArrayList<>();
        ArrayList<Future<DistrictResult>> futureDistrictResults = new ArrayList<>();

        logger.debug("District Assigning using " + THREAD_COUNT + " threads.");
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

    public static void shutdownThread() {
        executor.shutdownNow();
    }
}
