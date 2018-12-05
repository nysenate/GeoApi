package gov.nysenate.sage.service.geo;

import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.factory.SageThreadFactory;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.provider.geocode.RevGeocodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Service
public class ParallelRevGeocodeService
{
    private static Logger logger = LoggerFactory.getLogger(ParallelRevGeocodeService.class);
    private int THREAD_COUNT;
    private ExecutorService executor;
    private Environment env;

    @Autowired
    public ParallelRevGeocodeService(Environment env) {
        this.env = env;
        this.THREAD_COUNT = this.env.getValidateThreads();
        this.executor = Executors.newFixedThreadPool(THREAD_COUNT, new SageThreadFactory("revgeo"));
    }

    /**
    * Perform parallel reverse geocoding using the single reverse geocode implementation found in the
    * provided revGeocodeService.
    * @param revGeocodeService Reverse Geocode provider
    * @param points            List of points to lookup addresses for
    * @return                  ArrayList<GeocodeResult>
    */
    public ArrayList<GeocodeResult> reverseGeocode(RevGeocodeService revGeocodeService, List<Point> points)
    {
        ArrayList<GeocodeResult> revGeocodeResults = new ArrayList<>();
        ArrayList<Future<GeocodeResult>> futureRevGeocodeResults = new ArrayList<>();

        logger.trace("Reverse geocoding using " + THREAD_COUNT + " threads");
        for (Point point: points) {
            futureRevGeocodeResults.add(executor.submit(new ParallelRevGeocode(revGeocodeService, point)));
        }

        for (Future<GeocodeResult> geocodeResult : futureRevGeocodeResults) {
            try {
                revGeocodeResults.add(geocodeResult.get());
            }
            catch (InterruptedException ex) {
                logger.error(ex.getMessage());
            }
            catch (ExecutionException ex) {
                logger.error(ex.getMessage());
            }
        }
        return revGeocodeResults;
    }

    public void shutdownThread() {
        executor.shutdownNow();
    }

    /**
     * Callable for parallel reverse geocoding requests
     */
    private static class ParallelRevGeocode implements Callable<GeocodeResult>
    {
        public final RevGeocodeService revGeocodeService;
        public final Point point;
        public ParallelRevGeocode(RevGeocodeService revGeocodeService, Point point)
        {
            this.revGeocodeService = revGeocodeService;
            this.point = point;
        }

        @Override
        public GeocodeResult call()
        {
            return revGeocodeService.reverseGeocode(point);
        }
    }
}
