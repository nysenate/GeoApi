package gov.nysenate.sage.service.geo;

import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ParallelRevGeocodeService
{
    private static Logger logger = Logger.getLogger(ParallelRevGeocodeService.class);
    private static int THREAD_COUNT = 3;

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

    /**
    * Perform parallel reverse geocoding using the single reverse geocode implementation found in the
    * provided revGeocodeService.
    * @param revGeocodeService Reverse Geocode provider
    * @param points            List of points to lookup addresses for
    * @return                  ArrayList<GeocodeResult>
    */
    public static ArrayList<GeocodeResult> reverseGeocode(RevGeocodeService revGeocodeService, List<Point> points)
    {
        ArrayList<GeocodeResult> revGeocodeResults = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        ArrayList<Future<GeocodeResult>> futureRevGeocodeResults = new ArrayList<>();

        logger.debug("Reverse geocoding using " + THREAD_COUNT + " threads");
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
        executor.shutdown();
        return revGeocodeResults;
    }
}
