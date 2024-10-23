package gov.nysenate.sage.service.geo;

import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.provider.geocode.RevGeocodeService;
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

@Service
public class ParallelRevGeocodeService implements SageParallelRevGeocodeService {
    private static final Logger logger = LoggerFactory.getLogger(ParallelRevGeocodeService.class);
    private final int THREAD_COUNT;
    private final ThreadPoolTaskExecutor executor;

    @Autowired
    public ParallelRevGeocodeService(Environment env) {
        this.THREAD_COUNT = env.getValidateThreads();
        this.executor = ExecutorUtil.createExecutor("revgeo", THREAD_COUNT);
    }

    /**
    * Perform parallel reverse geocoding using the single reverse geocode implementation found in the
    * provided revGeocodeService.
    * @param revGeocodeService Reverse Geocode provider
    * @param points            List of points to lookup addresses for
    * @return                  ArrayList<GeocodeResult>
    */
    // TODO: not used?
    public List<GeocodeResult> reverseGeocode(RevGeocodeService revGeocodeService, List<Point> points) {
        ArrayList<GeocodeResult> revGeocodeResults = new ArrayList<>();
        ArrayList<Future<GeocodeResult>> futureRevGeocodeResults = new ArrayList<>();

        logger.trace("Reverse geocoding using {} threads", THREAD_COUNT);
        for (Point point: points) {
            futureRevGeocodeResults.add(executor.submit(new ParallelRevGeocode(revGeocodeService, point)));
        }

        for (Future<GeocodeResult> geocodeResult : futureRevGeocodeResults) {
            try {
                revGeocodeResults.add(geocodeResult.get());
            }
            catch (InterruptedException | ExecutionException ex) {
                logger.error(ex.getMessage());
            }
        }
        return revGeocodeResults;
    }

    public void shutdownThread() {
        executor.shutdown();
    }

    private record ParallelRevGeocode(RevGeocodeService revGeocodeService, Point point)
            implements Callable<GeocodeResult> {

        @Override
        public GeocodeResult call() {
            return revGeocodeService.reverseGeocode(point);
        }
    }
}
