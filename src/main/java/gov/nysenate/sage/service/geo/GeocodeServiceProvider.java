package gov.nysenate.sage.service.geo;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.provider.GeoCache;
import gov.nysenate.sage.service.base.ServiceProviders;
import gov.nysenate.sage.util.FormatUtil;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Point of access for all geocoding requests. This class maintains a collection of available
 * geocoding providers and contains logic for distributing requests and collecting responses
 * from the providers.
 */
public class GeocodeServiceProvider extends ServiceProviders<GeocodeService>
{
    private final Logger logger = Logger.getLogger(GeocodeServiceProvider.class);
    private final static String DEFAULT_GEO_PROVIDER = "yahoo";
    private final static LinkedList<String> DEFAULT_GEO_FALLBACK = new LinkedList<>(Arrays.asList("mapquest", "tiger"));
    private GeocodeCacheService geocodeCache;

    public GeocodeServiceProvider() {}

    /**
     * Returns a new instance of the geocode caching service.
     * @return GeocodeCacheService
     */
    public GeocodeCacheService newCacheInstance()
    {
        return new GeoCache();
    }

    /**
     * Perform a single geocode using the application defaults.
     * @param address   Address to geocode
     * @return          GeocodeResult
     */
    public GeocodeResult geocode(Address address)
    {
        return this.geocode(address, DEFAULT_GEO_PROVIDER, DEFAULT_GEO_FALLBACK, true, true);
    }

    /**
     * Perform a single geocode with caching and an optional default fallback mode.
     * @param address       Address to geocode
     * @param provider      Provider to perform geocoding
     * @param useFallback   Set true to use default fallback
     * @return              GeocodeResult
     */
    public GeocodeResult geocode(Address address, String provider, boolean useFallback)
    {
        return this.geocode(address, provider, DEFAULT_GEO_FALLBACK, useFallback, true);
    }

    /**
     * Perform a single geocode with all specified parameters.
     * @param address           Address to geocode
     * @param provider          Provider to perform geocoding
     * @param fallbackProviders Sequence of providers to fallback to
     * @param useFallback       Set true to use default fallback
     * @return                  GeocodeResult
     */
    GeocodeResult geocode(Address address, String provider, LinkedList<String> fallbackProviders, boolean useFallback,
                          boolean useCache)
    {
        this.geocodeCache = this.newCacheInstance();

        /** Clone the list of fall back providers */
        LinkedList<String> fallback = (fallbackProviders != null) ? new LinkedList<>(fallbackProviders)
                                                                  : new LinkedList<>(DEFAULT_GEO_FALLBACK);
        Iterator<String> fallbackIterator = fallback.iterator();
        GeocodeResult geocodeResult = (useCache) ? this.geocodeCache.geocode(address)
                                                 : new GeocodeResult(this.getClass(), ResultStatus.NO_GEOCODE_RESULT);
        boolean cacheHit = useCache && geocodeResult.isSuccess();
        logger.debug("Cache hit: " + cacheHit);

        /** Geocode using the supplied provider if valid */
        if (this.isRegistered(provider) && !cacheHit) {
            geocodeResult = this.newInstance(provider).geocode(address);
        }
        else {
            logger.error("Supplied an invalid geocoding provider!");
        }
        /** If attempt failed, use the fallback providers if allowed */
        if (!geocodeResult.isSuccess() && useFallback) {
            while (!geocodeResult.isSuccess() && fallbackIterator.hasNext()) {
                geocodeResult = this.newInstance(fallbackIterator.next()).geocode(address);
            }
        }
        /** Ensure we don't return a null response */
        if (geocodeResult == null) {
            geocodeResult = new GeocodeResult(this.getClass(), ResultStatus.NO_GEOCODE_RESULT);
        }
        /** Cache result */
        if (useCache && !cacheHit) {
            geocodeCache.saveToCache(geocodeResult);
        }
        return geocodeResult;
    }

    /**
     * Perform batch geocoding using recommended application defaults
     * @param addresses         List of addresses to geocode
     * @return                  List<GeocodeResult> corresponding to the addresses list.
     */
    public List<GeocodeResult> geocode(List<Address> addresses)
    {
        return this.geocode(addresses, DEFAULT_GEO_PROVIDER, DEFAULT_GEO_FALLBACK, true, true);
    }

    /**
     * Perform batch geocoding with default fallback option
     * @param addresses         List of addresses to geocode
     * @param provider          Provider to perform geocoding
     * @param useFallback       Set true to use default fallback
     * @return                  List<GeocodeResult> corresponding to the addresses list.
     */
    public List<GeocodeResult> geocode(List<Address> addresses, String provider, boolean useFallback)
    {
        return this.geocode(addresses, provider, DEFAULT_GEO_FALLBACK, useFallback, true);
    }

    /**
     * Perform batch geocoding with all specified parameters.
     * @param addresses         List of addresses to geocode
     * @param provider          Provider to perform geocoding
     * @param fallbackProviders Sequence of providers to fallback to
     * @param useFallback       Set true to use fallback
     * @return                  List<GeocodeResult> corresponding to the addresses list.
     */
    public List<GeocodeResult> geocode(List<Address> addresses, String provider,
                                       List<String> fallbackProviders, boolean useFallback, boolean useCache)
    {
        logger.info("Performing batch geocode using provider: " + provider + " with fallback to: " +
                    fallbackProviders + " with useFallback:" + useFallback + " and useCache:" + useCache);

        this.geocodeCache = this.newCacheInstance();

        /** Clone the list of fall back providers */
        LinkedList<String> fallback = (fallbackProviders != null) ? new LinkedList<>(fallbackProviders)
                                                                  : new LinkedList<>(DEFAULT_GEO_FALLBACK);
        List<GeocodeResult> geocodeResults = null;
        List<Integer> failedIndices;

        /** Clear out the fallback list if fallback is disabled */
        if (!useFallback) {
            fallback.clear();
        }
        /** If cache enabled, attempt to geocode batch and add the provider as the first fallback */
        if (useCache) {
            logger.debug("Running batch through geo cache..");
            geocodeResults = this.geocodeCache.geocode((ArrayList<Address>) addresses);
            fallback.add(0, provider);
        }
        /** Use the specified provider without cache */
        else if (this.isRegistered(provider)) {
            geocodeResults = this.newInstance(provider).geocode((ArrayList<Address>) addresses);
        }
        /** Throw an exception if provider is invalid with no fallbacks */
        else if (!useFallback || fallback.size() == 0) {
            throw new IllegalArgumentException("Supplied an invalid geocoding provider!");
        }

        /** Get the indices of results that were not successful */
        failedIndices = getFailedResultIndices(geocodeResults);
        logger.debug("There were " + failedIndices.size() + " cache misses.");

        Iterator<String> fallbackIterator = fallback.iterator();

        /** Create new batches containing just the failed results and run them through the fallback providers.
         *  Recompute the failed results and repeat until all fallback providers specified have been used. */
        while (!failedIndices.isEmpty() && fallbackIterator.hasNext()) {
            String fallbackProvider = fallbackIterator.next();
            logger.debug(failedIndices.size() + " geocodes are missing. Falling back to " + fallbackProvider);

            ArrayList<Address> fallbackBatch = new ArrayList<>();
            for (int failedIndex : failedIndices) {
                fallbackBatch.add(addresses.get(failedIndex));
            }

            List<GeocodeResult> fallbackResults = this.newInstance(fallbackProvider).geocode(fallbackBatch);
            Iterator<GeocodeResult> fallbackResultIterator = fallbackResults.iterator();
            for (int failedIndex : failedIndices) {
                geocodeResults.set(failedIndex, fallbackResultIterator.next());
            }
            failedIndices = getFailedResultIndices(geocodeResults);
        }

        /** Cache results */
        if (useCache) {
            geocodeCache.saveToCache(geocodeResults);
        }

        return geocodeResults;
    }

    /**
     * Retrieve a list of indices denoting the locations of failed results in the array.
     * @param geocodeResults    The results to analyze
     * @return List<Integer> containing failed indices
     */
    private List<Integer> getFailedResultIndices(List<GeocodeResult> geocodeResults)
    {
        List<Integer> failedIndices = new ArrayList<>();
        for (int i = 0; i < geocodeResults.size(); i++) {
            if (!geocodeResults.get(i).isSuccess()) {
                failedIndices.add(i);
            }
        }
        return failedIndices;
    }

    /**
     * Reverse geocoding uses the same strategy as <code>geocode</code>
     */
    public static GeocodeResult reverseGeocode(Point point)
    {
        return null;
    }

    /**
     * Reverse geocoding uses the same strategy as <code>geocode</code>
     */
    public static GeocodeResult reverseGeocode(Point point, String provider, boolean useFalback)
    {
        return null;
    }

    /**
     * Reverse geocoding uses the same strategy as <code>geocode</code>
     */
    public static GeocodeResult reverseGeocode(Point point, String provider, LinkedList<String> fallbackProviders,
                                               boolean useFallback)
    {
        return null;
    }
}
