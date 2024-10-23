package gov.nysenate.sage.service.geo;

import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.provider.geocode.GeocodeService;
import gov.nysenate.sage.util.ExecutorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * Parallel geocoding for use when a GeocodeService implementation does not provide
 * native batch methods.
 */
@Service
public class ParallelGeocodeService implements SageParallelGeocodeService {
    private static final Logger logger = LoggerFactory.getLogger(ParallelGeocodeService.class);
    private final int THREAD_COUNT;
    private final ThreadPoolTaskExecutor executor;

    @Autowired
    public ParallelGeocodeService(Environment env) {
        this.THREAD_COUNT = env.getValidateThreads();
        this.executor = ExecutorUtil.createExecutor("geocode", THREAD_COUNT);
    }

    @Override
    public List<GeocodeResult> geocode(GeocodeService geocodeService, List<Address> addresses) {
        List<GeocodeResult> geocodeResults = new ArrayList<>();
        List<Future<GeocodeResult>> futureGeocodeResults = new ArrayList<>();

        logger.trace("Geocoding using {} threads", THREAD_COUNT);
        for (Address address : addresses) {
            futureGeocodeResults.add(executor.submit(new ParallelGeocode(geocodeService, address)));
        }

        for (Future<GeocodeResult> geocodeResult : futureGeocodeResults) {
            try {
                geocodeResults.add(geocodeResult.get());
            }
            catch (Exception ex) {
                logger.error("{}", String.valueOf(ex));
            }
        }
        return geocodeResults;
    }

    private record ParallelGeocode(GeocodeService geocodeService, Address address) implements Callable<GeocodeResult> {
        @Override
        public GeocodeResult call() {
            return geocodeService.geocode(address);
        }
    }
}
