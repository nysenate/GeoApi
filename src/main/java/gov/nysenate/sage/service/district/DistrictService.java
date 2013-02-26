package gov.nysenate.sage.service.district;

import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.DistrictResult;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * DistrictService is used to assign district information to geocoded addresses.
 */
public interface DistrictService
{
    Logger logger = Logger.getLogger(DistrictService.class);

    public DistrictResult assignDistricts(GeocodedAddress geocodedAddress);
    public DistrictResult assignDistricts(GeocodedAddress geocodedAddress, List<DistrictType> types);
    public List<DistrictResult> assignDistricts(List<GeocodedAddress> geocodedAddresses);
    public List<DistrictResult> assignDistricts(List<GeocodedAddress> geocodedAddresses, List<DistrictType> types);

    /**
     * Parallel district assignment for use when a DistrictService implementation does not provide
     * native batch methods.
     */
    public class ParallelDistrictService
    {
        public class ParallelRequest implements Callable<DistrictResult>
        {
            public final DistrictService districtService;
            public final GeocodedAddress geocodedAddress;
            public final List<DistrictType> types;

            public ParallelRequest(DistrictService districtService, GeocodedAddress geocodedAddress, List<DistrictType> types)
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

        public List<DistrictResult> assignDistrictsParallel(DistrictService districtService, List<GeocodedAddress> geocodedAddresses, List<DistrictType> types)
        {
            ArrayList<DistrictResult> districtResults = new ArrayList<>();
            ExecutorService executor = Executors.newFixedThreadPool(5);
            ArrayList<Future<DistrictResult>> futureDistrictResults = new ArrayList<>();

            for (GeocodedAddress geocodedAddress : geocodedAddresses) {
                futureDistrictResults.add(executor.submit(new ParallelRequest(districtService, geocodedAddress, types)));
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
}
