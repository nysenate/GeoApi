package gov.nysenate.sage.service.geo;

import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.factory.SageThreadFactory;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.provider.geocode.GeocodeService;
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
 * Parallel geocoding for use when a GeocodeService implementation does not provide
 * native batch methods.
 */
@Service
public class ParallelGeocodeService
{
    private static Logger logger = LoggerFactory.getLogger(ParallelGeocodeService.class);
    private int THREAD_COUNT;
    private static ThreadPoolTaskExecutor executor;
    private Environment env;

    @Autowired
    public ParallelGeocodeService(Environment env) {
        this.env = env;
        this.THREAD_COUNT = this.env.getValidateThreads();
        this.executor = ExecutorUtil.createExecutor("geocode", THREAD_COUNT);
//        this.executor = Executors.newFixedThreadPool(THREAD_COUNT, new SageThreadFactory("geocode"));
    }

    public ArrayList<GeocodeResult> geocode(GeocodeService geocodeService, List<Address> addresses)
    {
        ArrayList<GeocodeResult> geocodeResults = new ArrayList<>();
        ArrayList<Future<GeocodeResult>> futureGeocodeResults = new ArrayList<>();

        logger.trace("Geocoding using " + THREAD_COUNT + " threads");
        for (Address address : addresses) {
            futureGeocodeResults.add(executor.submit(new ParallelGeocode(geocodeService, address)));
        }

        for (Future<GeocodeResult> geocodeResult : futureGeocodeResults) {
            try {
                geocodeResults.add(geocodeResult.get());
            }
            catch (Exception ex) {
                logger.error(ex + "");
            }
        }
        return geocodeResults;
    }

    public void shutdownThread() {
        executor.shutdown();
    }

    /**
     * Callable for parallel geocoding requests
     */
    private static class ParallelGeocode implements Callable<GeocodeResult>
    {
        public final GeocodeService geocodeService;
        public final Address address;
        public ParallelGeocode(GeocodeService geocodeService, Address address)
        {
            this.geocodeService = geocodeService;
            this.address = address;
        }

        @Override
        public GeocodeResult call()
        {
            return geocodeService.geocode(address);
        }
    }
}
