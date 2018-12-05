package gov.nysenate.sage.service.geo;

import gov.nysenate.sage.model.api.BatchGeocodeRequest;
import gov.nysenate.sage.model.api.GeocodeRequest;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.provider.district.DistrictService;
import gov.nysenate.sage.provider.geocode.GoogleGeocoder;
import gov.nysenate.sage.provider.geocode.NYSGeocoder;
import gov.nysenate.sage.provider.geocode.RevGeocodeService;
import gov.nysenate.sage.provider.geocode.TigerGeocoder;
import gov.nysenate.sage.service.base.ServiceProviders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.sql.Timestamp;
import java.util.*;

/**
* Point of access for all reverse geocoding requests.
*/
@Service
public class RevGeocodeServiceProvider
{
    private final Logger logger = LoggerFactory.getLogger(GeocodeServiceProvider.class);

    protected RevGeocodeService defaultProvider;
    protected Map<String,RevGeocodeService> providers = new HashMap<>();
    protected LinkedList<String> defaultFallback = new LinkedList<>();

    @Autowired
    public RevGeocodeServiceProvider(GoogleGeocoder googleGeocoder, TigerGeocoder tigerGeocoder,
                                     NYSGeocoder nysGeocoder) {
        this.defaultProvider = googleGeocoder;
        this.providers.put("google", googleGeocoder);
        this.providers.put("nysgeo", nysGeocoder);
        this.providers.put("tiger", tigerGeocoder);
        this.defaultFallback.add("nysgeo");
        this.defaultFallback.add("tiger");
    }

    public GeocodeResult reverseGeocode(GeocodeRequest geocodeRequest)
    {
        if (geocodeRequest != null) {
            return reverseGeocode(geocodeRequest.getPoint(), this.providers.get(geocodeRequest.getProvider()), geocodeRequest.isUseFallback());
        }
        else {
            return null;
        }
    }

    /**
     * Perform reverse geocode with default options.
     * @param point Point to lookup address for
     * @return      GeocodeResult
     */
    public GeocodeResult reverseGeocode(Point point)
    {
        return reverseGeocode(point, this.defaultProvider, this.defaultFallback, true);
    }

    /**
     * Perform reverse geocode with provider and fallback options.
     * @param point        Point to lookup address for
     * @param provider     Provider to perform reverse geocoding
     * @param useFallback  Set true to use default fallback
     * @return             GeocodeResult
     */
    public GeocodeResult reverseGeocode(Point point, RevGeocodeService provider, boolean useFallback)
    {
        return reverseGeocode(point, provider, this.defaultFallback, useFallback);
    }

    /**
     * Perform reverse geocoding with all options specified.
     * @param point             Point to lookup address for
     * @param provider          Provider to perform reverse geocoding
     * @param fallbackProviders Sequence of providers to fallback to
     * @param useFallback       Set true to allow fallback
     * @return                  GeocodeResult
     */
    public GeocodeResult reverseGeocode(Point point, RevGeocodeService provider, LinkedList<String> fallbackProviders,
                                        boolean useFallback)
    {
        logger.debug("Performing reverse geocode on point " + point);
        GeocodeResult geocodeResult = new GeocodeResult(this.getClass(), ResultStatus.NO_REVERSE_GEOCODE_RESULT);
        /** Clone the list of fall back reverse geocode providers */
        LinkedList<String> fallback = (fallbackProviders != null) ? new LinkedList<>(fallbackProviders)
                                                                  : new LinkedList<>(this.defaultFallback);

        if (provider != null && !this.providers.containsKey(provider)) {
            geocodeResult = this.providers.get(provider).reverseGeocode(point);
        }

        if (!geocodeResult.isSuccess() && useFallback) {
            Iterator<String> fallbackIterator = fallback.iterator();
            while (!geocodeResult.isSuccess() && fallbackIterator.hasNext()) {
                geocodeResult = this.providers.get(fallbackIterator.next()).reverseGeocode(point);
            }
        }
        geocodeResult.setResultTime(new Timestamp(new Date().getTime()));
        return geocodeResult;
    }

    /**
     * Perform batch reverse geocoding using supploed BatchGeocodeRequest with points set.
     * @param batchRevGeoRequest
     * @return  List<GeocodeResult> or null if batchRevGeoRequest is null.
     */
    public List<GeocodeResult> reverseGeocode(BatchGeocodeRequest batchRevGeoRequest)
    {
        if (batchRevGeoRequest != null) {
            return reverseGeocode(batchRevGeoRequest.getPoints(), this.providers.get(batchRevGeoRequest.getProvider()), this.defaultFallback);
        }
        return null;
    }

    /**
    * Perform batch reverse geocoding.
    */
    public List<GeocodeResult> reverseGeocode(List<Point> points)
    {
        return reverseGeocode(points, this.defaultProvider, this.defaultFallback);
    }

    /**
    * Perform batch reverse geocoding.
    */
    public List<GeocodeResult> reverseGeocode(List<Point> points, RevGeocodeService provider)
    {
        return reverseGeocode(points, provider, this.defaultFallback);
    }

    /**
    * For batch reverse geocode, simply call the single reverse geocode method iteratively.
    * Keeping things simple because performance here isn't that crucial.
    */
    public List<GeocodeResult> reverseGeocode(List<Point> points, RevGeocodeService provider, LinkedList<String> fallbackProviders)
    {
        List<GeocodeResult> geocodeResults = new ArrayList<>();
        for (Point point : points) {
            geocodeResults.add(reverseGeocode(point, provider, fallbackProviders, true));
        }
        return geocodeResults;
    }
}
