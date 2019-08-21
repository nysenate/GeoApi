package gov.nysenate.sage.service.geo;

import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.provider.geocode.RevGeocodeService;

import java.util.ArrayList;
import java.util.List;

public interface SageParallelRevGeocodeService {
    /**
     * Perform parallel reverse geocoding using the single reverse geocode implementation found in the
     * provided revGeocodeService.
     * @param revGeocodeService Reverse Geocode provider
     * @param points            List of points to lookup addresses for
     * @return                  ArrayList<GeocodeResult>
     */
    public ArrayList<GeocodeResult> reverseGeocode(RevGeocodeService revGeocodeService, List<Point> points);

    /**
     * Shuts down the parallel threads performing the reverse geocodes
     */
    public void shutdownThread();
}
