package gov.nysenate.sage.service.district;

import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.DistrictResult;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Parallel district assignment for use when a DistrictService implementation does not provide
 * native batch methods.
 */
public abstract class ParallelDistrictService
{
    private static Logger logger = Logger.getLogger(ParallelDistrictService.class);
    private static int THREAD_COUNT = 5;

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
            return districtService.assignDistricts(geocodedAddress, types);
        }
    }

    public static List<DistrictResult> assignDistricts(DistrictService districtService, List<GeocodedAddress> geocodedAddresses, List<DistrictType> types)
    {
        ArrayList<DistrictResult> districtResults = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        ArrayList<Future<DistrictResult>> futureDistrictResults = new ArrayList<>();

        logger.debug("District Assigning using " + THREAD_COUNT + " threads");
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
        executor.shutdown();
        return districtResults;
    }
}
