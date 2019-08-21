package gov.nysenate.sage.service.geo;

import gov.nysenate.sage.model.api.BatchGeocodeRequest;
import gov.nysenate.sage.model.api.GeocodeRequest;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.provider.geocode.RevGeocodeService;

import java.util.LinkedList;
import java.util.List;

public interface SageRevGeocodeServiceProvider {

    /**
     * Perform a reverse geocode on the submitted options
     * @param geocodeRequest
     * @return
     */
    public GeocodeResult reverseGeocode(GeocodeRequest geocodeRequest);

    /**
     * Perform reverse geocode with default options.
     * @param point Point to lookup address for
     * @return      GeocodeResult
     */
    public GeocodeResult reverseGeocode(Point point);

    /**
     * Perform reverse geocode with provider and fallback options.
     * @param point        Point to lookup address for
     * @param provider     Provider to perform reverse geocoding
     * @param useFallback  Set true to use default fallback
     * @return             GeocodeResult
     */
    public GeocodeResult reverseGeocode(Point point, RevGeocodeService provider, boolean useFallback);

    /**
     * Perform reverse geocoding with all options specified.
     * @param point             Point to lookup address for
     * @param provider          Provider to perform reverse geocoding
     * @param fallbackProviders Sequence of providers to fallback to
     * @param useFallback       Set true to allow fallback
     * @return                  GeocodeResult
     */
    public GeocodeResult reverseGeocode(Point point, RevGeocodeService provider, LinkedList<String> fallbackProviders,
                                        boolean useFallback);

    /**
     * Perform batch reverse geocoding using supploed BatchGeocodeRequest with points set.
     * @param batchRevGeoRequest
     * @return  List<GeocodeResult> or null if batchRevGeoRequest is null.
     */
    public List<GeocodeResult> reverseGeocode(BatchGeocodeRequest batchRevGeoRequest);

    /**
     * Perform batch reverse geocoding.
     */
    public List<GeocodeResult> reverseGeocode(List<Point> points);

    /**
     * Perform batch reverse geocoding.
     */
    public List<GeocodeResult> reverseGeocode(List<Point> points, RevGeocodeService provider);

    /**
     * For batch reverse geocode, simply call the single reverse geocode method iteratively.
     * Keeping things simple because performance here isn't that crucial.
     */
    public List<GeocodeResult> reverseGeocode(List<Point> points, RevGeocodeService provider, LinkedList<String> fallbackProviders);

}
