package gov.nysenate.sage.service.geo;

import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.service.base.ServiceProviders;
import gov.nysenate.sage.util.Config;
import org.apache.log4j.Logger;

import java.util.*;

/**
* Point of access for all reverse geocoding requests.
*/
public class RevGeocodeServiceProvider extends ServiceProviders<RevGeocodeService> implements Observer
{
    private final Logger logger = Logger.getLogger(GeocodeServiceProvider.class);
    private final static Config config = ApplicationFactory.getConfig();

    private final static String DEFAULT_GEO_PROVIDER = "yahoo";
    private final static LinkedList<String> DEFAULT_GEO_FALLBACK = new LinkedList<>(Arrays.asList("mapquest", "tiger"));

    public RevGeocodeServiceProvider() {}

    @Override
    public void update(Observable o, Object arg) {}

    /**
     * Perform reverse geocode with default options.
     * @param point Point to lookup address for
     * @return      GeocodeResult
     */
    public GeocodeResult reverseGeocode(Point point)
    {
        return reverseGeocode(point, DEFAULT_GEO_PROVIDER, DEFAULT_GEO_FALLBACK, true);
    }

    /**
     * Perform reverse geocode with provider and fallback options.
     * @param point        Point to lookup address for
     * @param provider     Provider to perform reverse geocoding
     * @param useFallback  Set true to use default fallback
     * @return             GeocodeResult
     */
    public GeocodeResult reverseGeocode(Point point, String provider, boolean useFallback)
    {
        return reverseGeocode(point, provider, DEFAULT_GEO_FALLBACK, useFallback);
    }

    /**
     * Perform reverse geocoding with all options specified.
     * @param point             Point to lookup address for
     * @param provider          Provider to perform reverse geocoding
     * @param fallbackProviders Sequence of providers to fallback to
     * @param useFallback       Set true to allow fallback
     * @return                  GeocodeResult
     */
    public GeocodeResult reverseGeocode(Point point, String provider, LinkedList<String> fallbackProviders,
                                        boolean useFallback)
    {
        GeocodeResult geocodeResult = new GeocodeResult(this.getClass(), ResultStatus.NO_REVERSE_GEOCODE_RESULT);
        if (provider != null && !provider.isEmpty()) {
            geocodeResult = this.newInstance(provider).reverseGeocode(point);
        }
        if (!geocodeResult.isSuccess() && useFallback) {

            /** Clone the list of fall back reverse geocode providers */
            LinkedList<String> fallback = (fallbackProviders != null) ? new LinkedList<>(fallbackProviders)
                    : new LinkedList<>(DEFAULT_GEO_FALLBACK);

            Iterator<String> fallbackIterator = fallback.iterator();
            while (!geocodeResult.isSuccess() && fallbackIterator.hasNext()) {
                geocodeResult = this.newInstance(fallbackIterator.next()).reverseGeocode(point);
            }
        }
        return geocodeResult;
    }

    /**
    * Perform batch reverse geocoding.
    */
    public List<GeocodeResult> reverseGeocode(List<Point> points)
    {
        return reverseGeocode(points, DEFAULT_GEO_PROVIDER, DEFAULT_GEO_FALLBACK);
    }

    /**
    * Perform batch reverse geocoding.
    */
    public List<GeocodeResult> reverseGeocode(List<Point> points, String provider)
    {
        return reverseGeocode(points, provider, DEFAULT_GEO_FALLBACK);
    }

    /**
    * For batch reverse geocode, simply call the single reverse geocode method iteratively.
    * Keeping things simple because performance here isn't that crucial.
    */
    public List<GeocodeResult> reverseGeocode(List<Point> points, String provider, LinkedList<String> fallbackProviders)
    {
        List<GeocodeResult> geocodeResults = new ArrayList<>();
        for (Point point : points) {
            geocodeResults.add(reverseGeocode(point, provider, fallbackProviders, true));
        }
        return geocodeResults;
    }
}
