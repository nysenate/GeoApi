package gov.nysenate.sage.service.geo;

import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.api.BatchGeocodeRequest;
import gov.nysenate.sage.model.api.GeocodeRequest;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.provider.GeoCache;
import gov.nysenate.sage.provider.GoogleGeocoder;
import gov.nysenate.sage.provider.TigerGeocoder;
import gov.nysenate.sage.service.base.ServiceProviders;
import gov.nysenate.sage.util.TimeUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

/**
 * Point of access for all geocoding requests. This class maintains a collection of available
 * geocoding providers and contains logic for distributing requests and collecting responses
 * from the providers.
 */
@Service
public class GeocodeServiceProvider extends ServiceProviders<GeocodeService> implements Observer
{
    private final Logger logger = LogManager.getLogger(GeocodeServiceProvider.class);
    private final Environment env;
    private Map<String, Class<? extends GeocodeService>> activeGeoProviders = new HashMap<>();

    /** Caching members */
    private GeocodeCacheService geocodeCache;
    private boolean CACHE_ENABLED = true;

    @Autowired
    public GeocodeServiceProvider(Environment env, GeocodeCacheService geocodeCache) {
        this.env = env;
        this.geocodeCache = geocodeCache;
        /** Setup geocode service providers. */
        Map<String, Class<? extends GeocodeService>> geoProviders = new HashMap<>();
        geoProviders.put("google", GoogleGeocoder.class);
        geoProviders.put("tiger", TigerGeocoder.class);

        for (String key : geoProviders.keySet()) {
            logger.debug("Adding geocoder: " + key);
            registerProvider(key, geoProviders.get(key));
        }

        String[] activeList = env.getGeocoderActive().split(",");
        for (String provider : activeList) {
            GeocodeServiceValidator.setGeocoderAsActive(geoProviders.get(provider));
            activeGeoProviders.put(provider, geoProviders.get(provider));
        }

        LinkedList<String> geocoderRankList = new LinkedList<>(Arrays.asList(env.getGeocoderRank().split(",")));
        if (!geocoderRankList.isEmpty()) {
            /** Set the first geocoder as the default. */
            setDefaultProvider(geocoderRankList.removeFirst());
            /** Set the fallback chain in the order of the ranking (excluding first). */
            setProviderFallbackChain(geocoderRankList);
        }

        /** Designate which geocoders are allowed to cache. */
        List<String> cacheableProviderList = Arrays.asList(env.getGeocoderCacheable().split(","));
        for (String provider : cacheableProviderList) {
            registerProviderAsCacheable(provider);
        }

        update(null, null);
    }

    @Override
    public void update(Observable o, Object arg) {
        CACHE_ENABLED = env.getGeocahceEnabled();
    }

    /**
     * Designates a provider (that has been registered) as a reliable source for caching results.
     * @param providerName Same providerName used when registering the provider
     */
    public void registerProviderAsCacheable(String providerName)
    {
        GeocodeService provider = this.getInstance(providerName);
        GeoCache.registerProviderAsCacheable(provider.getClass());
    }

    /**
     * Performs single geocode using GeocodeRequest.
     * @param geocodeRequest GeocodeRequest object with geocode options and address set.
     * @return GeocodeResult
     */
    public GeocodeResult geocode(GeocodeRequest geocodeRequest)
    {
        if (geocodeRequest != null) {
            String provider = (geocodeRequest.getProvider() != null && !geocodeRequest.getProvider().isEmpty())
                              ? geocodeRequest.getProvider() : this.defaultProvider;
            return this.geocode(geocodeRequest.getAddress(), provider, this.defaultFallback,
                                geocodeRequest.isUseFallback(), geocodeRequest.isUseCache());
        }
        return null;
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
     * @param useCache          Set true to attempt cache lookup first
     * @return                  GeocodeResult
     */
    public GeocodeResult geocode(Address address, String provider, LinkedList<String> fallbackProviders, boolean useFallback,
                          boolean useCache)
    {
        /** Clone the list of fall back providers */
        LinkedList<String> fallback = (fallbackProviders != null) ? new LinkedList<>(fallbackProviders)
                                                                  : new LinkedList<>(this.defaultFallback);
        Timestamp startTime = TimeUtil.currentTimestamp();
        GeocodeResult geocodeResult = (CACHE_ENABLED && useCache) ? this.geocodeCache.geocode(address)
                                                 : new GeocodeResult(this.getClass(), ResultStatus.NO_GEOCODE_RESULT);
        boolean cacheHit = CACHE_ENABLED && useCache && geocodeResult.isSuccess();

        if (!cacheHit) {
            /** Geocode using the supplied provider if valid */
            if (this.isRegistered(provider)) {
                /** Remove the provider if it's set in the fallback chain */
                fallback.remove(provider);
                startTime = TimeUtil.currentTimestamp();
                geocodeResult = this.getInstance(provider).geocode(address);
                logger.info(String.format("%s response time: %d ms.", provider, TimeUtil.getElapsedMs(startTime)));
            }
            else {
                logger.error("Supplied an invalid geocoding provider! " + provider);
                if (useFallback && !fallback.contains(this.defaultProvider)) {
                    logger.info("Adding default geocoder as fallback");
                    fallback.set(0, this.defaultProvider);
                }
            }
        }
        else {
            logger.info(String.format("Cache hit in %d ms.", TimeUtil.getElapsedMs(startTime)));
        }

        /** If attempt failed, use the fallback providers if allowed */
        if (geocodeResult != null && !geocodeResult.isSuccess() && useFallback) {
            Iterator<String> fallbackIterator = fallback.iterator();
            while (!geocodeResult.isSuccess() && fallbackIterator.hasNext()) {
                provider = fallbackIterator.next();
                logger.info(String.format("Sending through %s.", provider));
                startTime = TimeUtil.currentTimestamp();
                geocodeResult = this.getInstance(provider).geocode(address);
                logger.info(String.format("%s response time: %d ms.", provider, TimeUtil.getElapsedMs(startTime)));
            }
        }
        /** Ensure we don't return a null response */
        if (geocodeResult == null || !geocodeResult.isSuccess()) {
            logger.warn("No valid geocode result.");
            geocodeResult = new GeocodeResult(this.getClass(), ResultStatus.NO_GEOCODE_RESULT);
        }
        /** Output result if log level is high enough */
        else if (logger.isDebugEnabled()) {
            Geocode gc = geocodeResult.getGeocode();
            Address addr = geocodeResult.getAddress();
            if (gc != null && addr != null) {
                String source = (geocodeResult.getSource() != null) ? geocodeResult.getSource().getSimpleName() : "Missing";
                logger.debug(String.format("Geocode Result - Source: '%s', Quality: '%s', Lat/Lon: '%s', Address: '%s'",
                    source, gc.getQuality(), gc.getLatLon(), addr));
            }
        }

        /** Set the timestamp */
        geocodeResult.setResultTime(TimeUtil.currentTimestamp());

        /** Cache result */
        if (CACHE_ENABLED && !cacheHit) {
            geocodeCache.saveToCacheAndFlush(geocodeResult);
        }
        return geocodeResult;
    }

    /**
     * Perform batch geocoding using supplied BatchGeocodeRequest
     * @param batchGeoRequest BatchGeocodeRequest with desired fields set
     * @return  List<GeocodeResult> corresponding to the addresses list
     */
    public List<GeocodeResult> geocode(BatchGeocodeRequest batchGeoRequest)
    {
        if (batchGeoRequest != null) {
            String provider = (batchGeoRequest.getProvider() != null && !batchGeoRequest.getProvider().isEmpty())
                              ? batchGeoRequest.getProvider()
                              : this.defaultProvider;
            return this.geocode(batchGeoRequest.getAddresses(), provider, batchGeoRequest.isUseFallback(),
                                batchGeoRequest.isUseCache());
        }
        return null;
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
     * @param useCache          Set true to attempt cache lookup first
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
     * @param useCache          Set true to attempt cache lookup first
     * @return                  List<GeocodeResult> corresponding to the addresses list.
     */
    public List<GeocodeResult> geocode(List<Address> addresses, String provider,
                                       List<String> fallbackProviders, boolean useFallback, boolean useCache)
    {

        long cacheElapsedMs = 0;
        final int addressCount = addresses.size();

        /** Clone the list of fall back providers */
        LinkedList<String> fallback = (fallbackProviders != null) ? new LinkedList<>(fallbackProviders)
                                                                  : new LinkedList<>(this.defaultFallback);
        List<GeocodeResult> geocodeResults = new ArrayList<>();
        logger.info(String.format("Performing %d geocodes.", addressCount));

        /** Make note of the indices that contain empty addresses and create a new list of addresses
        * containing just the addresses with values. */
        List<Address> validAddresses = new ArrayList<>(addressCount);
        List<Integer> invalidIndices = new ArrayList<>();
        for (int i = 0; i < addressCount; i++) {
            if (addresses.get(i) != null && !addresses.get(i).isEmpty()) {
                validAddresses.add(addresses.get(i));
            }
            else {
                invalidIndices.add(i);
            }
        }

        /** Clear out the fallback list if fallback is disabled */
        if (!useFallback) {
            fallback.clear();
        }
        /** If cache enabled, attempt to geocode batch and add the provider as the first fallback */
        if (CACHE_ENABLED && useCache) {
            logger.debug("Running batch through geo cache..");
            Timestamp benchmark1 = TimeUtil.currentTimestamp();

            geocodeResults = this.geocodeCache.geocode((ArrayList<Address>) validAddresses);
            cacheElapsedMs = TimeUtil.getElapsedMs(benchmark1);

            if (!fallback.contains(provider)) {
                fallback.add(0, provider);
            }
        }
        /** Use the specified provider without cache */
        else if (this.isRegistered(provider)) {
            /** Remove the provider if it's set in the fallback chain */
            fallback.remove(provider);
            logger.info(String.format("Skipped cache lookup. Using %s.", provider));
            geocodeResults = this.getInstance(provider).geocode((ArrayList<Address>) validAddresses);
        }
        /** Otherwise populate the results array with failed results so they get picked up
         *  during the fallback stage. */
        else if (useFallback && fallback.size() > 0) {
            for (int i = 0; i < addressCount; i++) {
                geocodeResults.add(new GeocodeResult(this.getClass(), ResultStatus.NO_GEOCODE_RESULT));
            }
        }
        /** Throw an exception if provider is invalid with no fallbacks */
        else {
            throw new IllegalArgumentException("Supplied an invalid geocoding provider with no cache or fallback!");
        }

        /** Get the indices of results that were not successful */
        List<Integer> failedIndices = getFailedResultIndices(geocodeResults);
        if (CACHE_ENABLED && useCache) {
            logger.info(String.format("Cache hits: %d/%d. Lookup time: %d ms.",
                (validAddresses.size() - failedIndices.size()), validAddresses.size(), cacheElapsedMs));
        }

        /** Create new batches containing just the failed results and run them through the fallback providers.
         *  Recompute the failed results and repeat until all fallback providers specified have been used. */
        Iterator<String> fallbackIterator = fallback.iterator();
        while (!failedIndices.isEmpty() && fallbackIterator.hasNext()) {
            provider = fallbackIterator.next();
            logger.info(failedIndices.size() + " geocodes remaining. Sending through " + provider + ".");

            ArrayList<Address> fallbackBatch = new ArrayList<>();
            for (int failedIndex : failedIndices) {
                fallbackBatch.add(validAddresses.get(failedIndex));
            }

            Timestamp startTime = TimeUtil.currentTimestamp();
            List<GeocodeResult> fallbackResults = this.getInstance(provider).geocode(fallbackBatch);
            long elapsedMs = TimeUtil.getElapsedMs(startTime);
            String responseTimeMsg = String.format("%s response time: %d ms.", provider, elapsedMs);
            if (elapsedMs > 10000) {
                logger.warn(responseTimeMsg);
            }
            else {
                logger.info(responseTimeMsg);
            }

            Iterator<GeocodeResult> fallbackResultIterator = fallbackResults.iterator();
            for (int failedIndex : failedIndices) {
                GeocodeResult fallbackResult = fallbackResultIterator.next();
                if (fallbackResult != null) {
                    geocodeResults.set(failedIndex, fallbackResult);
                }
            }
            failedIndices = getFailedResultIndices(geocodeResults);
        }

        if (!failedIndices.isEmpty()) {
            logger.info(String.format("%d addresses were not geocoded!", failedIndices.size()));
        }

        /** If the geocodeResults do not align with the valid input address set, produce error and return empty array. */
        if (geocodeResults.size() != validAddresses.size()) {
            logger.error("Batch geocode result size not equal to address set size!");
            GeocodeResult errorResult = new GeocodeResult(this.getClass(), ResultStatus.INTERNAL_ERROR);
            errorResult.addMessage("Batch response was not consistent with the input addresses list.");
            return new ArrayList<>(Arrays.asList(errorResult));
        }

        /** Create a final result array and set it to geocodeResults if no addresses were
         *  empty or iterate through and insert the failed results in the proper order. */
        List<GeocodeResult> finalGeocodeResults;
        if (!invalidIndices.isEmpty()) {
            finalGeocodeResults = new ArrayList<>(addresses.size());
            Iterator<GeocodeResult> geoResultIterator = geocodeResults.iterator();
            for (int i = 0; i < addressCount; i++) {
                if (invalidIndices.contains(i)) {
                    GeocodeResult invalidInputResult = new GeocodeResult(this.getClass(), ResultStatus.INSUFFICIENT_ADDRESS,
                                                                         new GeocodedAddress(addresses.get(i)));
                    finalGeocodeResults.add(invalidInputResult);
                }
                else {
                    finalGeocodeResults.add(geoResultIterator.next());
                }
            }
        }
        else {
            finalGeocodeResults = geocodeResults;
        }

        /** Loop through results and set the timestamp */
        for (GeocodeResult geocodeResult : finalGeocodeResults) {
            if (geocodeResult != null) {
                geocodeResult.setResultTime(TimeUtil.currentTimestamp());
            }
        }

        /** Cache results */
        if (CACHE_ENABLED && useCache) {
            geocodeCache.saveToCacheAndFlush(finalGeocodeResults);
        }

        return finalGeocodeResults;
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

    public Map<String, Class<? extends GeocodeService>> getActiveGeoProviders() {
        return activeGeoProviders;
    }
}
