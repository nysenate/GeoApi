package gov.nysenate.sage.service.geo;

import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.provider.GeoCache;
import gov.nysenate.sage.service.base.ServiceProviders;
import gov.nysenate.sage.util.Config;
import org.apache.log4j.Logger;

import java.sql.Timestamp;
import java.util.*;

/**
 * Point of access for all geocoding requests. This class maintains a collection of available
 * geocoding providers and contains logic for distributing requests and collecting responses
 * from the providers.
 */
public class GeocodeServiceProvider extends ServiceProviders<GeocodeService> implements Observer
{
    private final Logger logger = Logger.getLogger(GeocodeServiceProvider.class);
    private final static Config config = ApplicationFactory.getConfig();

    /** Caching members */
    private static List<String> cacheableProviders = new ArrayList<>();
    private GeocodeCacheService geocodeCache;
    private Boolean CACHE_ENABLED = true;

    public GeocodeServiceProvider() {
        config.notifyOnChange(this);
        update(null, null);
    }

    @Override
    public void update(Observable o, Object arg) {
        CACHE_ENABLED = Boolean.parseBoolean(config.getValue("cache.enabled"));
    }

    /**
     * Designates a provider (that has been registered) as a reliable source for caching results.
     * @param providerName Same providerName used when registering the provider
     */
    public void registerProviderAsCacheable(String providerName)
    {
        cacheableProviders.add(providerName);
    }

    /**
     * Checks if providerName is allowed to save result into cache
     * @param providerName
     * @return true if it is allowed, false otherwise
     */
    public boolean isProviderCacheable(String providerName)
    {
        return cacheableProviders.contains(providerName);
    }

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
        return this.geocode(address, this.defaultProvider, this.defaultFallback, true, true);
    }

    /**
     * Perform a single geocode with optional default fallback mode.
     * @param address       Address to geocode
     * @param provider      Provider to perform geocoding
     * @param useFallback   Set true to use default fallback
     * @return              GeocodeResult
     */
    public GeocodeResult geocode(Address address, String provider, boolean useFallback)
    {
        if (provider == null || provider.isEmpty()) {
            provider = this.defaultProvider;
        }
        return this.geocode(address, provider, useFallback, true);
    }

    /**
     * Perform a single geocode with caching and an optional default fallback mode.
     * @param address       Address to geocode
     * @param provider      Provider to perform geocoding
     * @param useFallback   Set true to use default fallback
     * @return              GeocodeResult
     */
    public GeocodeResult geocode(Address address, String provider, boolean useFallback, boolean useCache)
    {
        if (provider == null || provider.isEmpty()) {
            provider = this.defaultProvider;
        }
        return this.geocode(address, provider, this.defaultFallback, useFallback, useCache);
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
        /** Clone the list of fall back providers */
        LinkedList<String> fallback = (fallbackProviders != null) ? new LinkedList<>(fallbackProviders)
                                                                  : new LinkedList<>(this.defaultFallback);
        /** Set up and hit the cache */
        this.geocodeCache = this.newCacheInstance();
        GeocodeResult geocodeResult = (CACHE_ENABLED && useCache) ? this.geocodeCache.geocode(address)
                                                 : new GeocodeResult(this.getClass(), ResultStatus.NO_GEOCODE_RESULT);
        boolean cacheHit = CACHE_ENABLED && useCache && geocodeResult.isSuccess();
        logger.debug((CACHE_ENABLED) ? "Cache hit: " + cacheHit : "Cache disabled");

        if (!cacheHit) {
            /** Geocode using the supplied provider if valid */
            if (this.isRegistered(provider)) {
                geocodeResult = this.newInstance(provider).geocode(address);
            }
            else {
                logger.error("Supplied an invalid geocoding provider! " + provider);
                if (useFallback && !fallbackProviders.contains(this.defaultProvider)) {
                    logger.info("Adding default geocoder as fallback");
                    fallbackProviders.set(0, this.defaultProvider);
                }
            }
        }
        /** If attempt failed, use the fallback providers if allowed */
        if (!geocodeResult.isSuccess() && useFallback) {
            Iterator<String> fallbackIterator = fallback.iterator();
            while (!geocodeResult.isSuccess() && fallbackIterator.hasNext()) {
                provider = fallbackIterator.next();
                geocodeResult = this.newInstance(provider).geocode(address);
            }
        }
        /** Ensure we don't return a null response */
        if (geocodeResult == null) {
            geocodeResult = new GeocodeResult(this.getClass(), ResultStatus.NO_GEOCODE_RESULT);
        }

        /** Set the timestamp */
        geocodeResult.setResultTime(new Timestamp(new Date().getTime()));

        /** Cache result */
        if (CACHE_ENABLED && !cacheHit && isProviderCacheable(provider)) {
            geocodeCache.saveToCacheAndFlush(geocodeResult);
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
        return this.geocode(addresses, this.defaultProvider, this.defaultFallback, true, true);
    }

    /**
     * Perform batch geocoding with default fallback option
     * @param addresses         List of addresses to geocode
     * @param provider          Provider to perform geocoding
     * @param useFallback       Set true to use default fallback
     * @return                  List<GeocodeResult> corresponding to the addresses list.
     */
    public List<GeocodeResult> geocode(List<Address> addresses, String provider, boolean useFallback, boolean useCache)
    {
        return this.geocode(addresses, provider, this.defaultFallback, useFallback, true);
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
                                                                  : new LinkedList<>(this.defaultFallback);
        List<GeocodeResult> geocodeResults = null;
        List<Integer> failedIndices;

        /** Clear out the fallback list if fallback is disabled */
        if (!useFallback) {
            fallback.clear();
        }
        /** If cache enabled, attempt to geocode batch and add the provider as the first fallback */
        if (CACHE_ENABLED && useCache) {
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

        /** Loop through results and set the timestamp */
        for (GeocodeResult geocodeResult : geocodeResults) {
            if (geocodeResult != null) {
                geocodeResult.setResultTime(new Timestamp(new Date().getTime()));
            }
        }

        /** Cache results */
        if (CACHE_ENABLED && useCache) {
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
}
