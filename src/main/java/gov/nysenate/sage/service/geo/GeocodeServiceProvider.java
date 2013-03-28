package gov.nysenate.sage.service.geo;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.service.base.ServiceProviders;
import gov.nysenate.sage.util.FormatUtil;
import org.apache.log4j.Logger;

import java.util.*;

public class GeocodeServiceProvider extends ServiceProviders<GeocodeService>
{
    private final Logger logger = Logger.getLogger(GeocodeServiceProvider.class);
    private final static String DEFAULT_GEO_PROVIDER = "yahoo";
    private final static LinkedList<String> DEFAULT_GEO_FALLBACK = new LinkedList<>(Arrays.asList("mapquest", "tiger"));

    /**
     * Perform a single geocode using the application defaults.
     * @param address   Address to geocode
     * @return          GeocodeResult
     */
    public GeocodeResult geocode(Address address)
    {
        return this.geocode(address, DEFAULT_GEO_PROVIDER, DEFAULT_GEO_FALLBACK, true);
    }

    /**
     * Perform a single geocode with an optional default fallback mode.
     * @param address       Address to geocode
     * @param provider      Provider to perform geocoding
     * @param useFallback   Set true to use default fallback
     * @return              GeocodeResult
     */
    public GeocodeResult geocode(Address address, String provider, boolean useFallback)
    {
        return this.geocode(address, provider, DEFAULT_GEO_FALLBACK, useFallback);
    }

    /**
     * Perform a single geocode with all specified parameters.
     * @param address           Address to geocode
     * @param provider          Provider to perform geocoding
     * @param fallbackProviders Sequence of providers to fallback to
     * @param useFallback       Set true to use default fallback
     * @return                  GeocodeResult
     */
    GeocodeResult geocode(Address address, String provider, LinkedList<String> fallbackProviders, boolean useFallback)
    {
        /** Clone the list of fall back providers */
        LinkedList<String> fallback = (fallbackProviders != null) ? (LinkedList<String>) fallbackProviders.clone() :
                                                                    (LinkedList<String>) DEFAULT_GEO_FALLBACK.clone();
        Iterator<String> fallbackIterator = fallback.iterator();
        GeocodeResult geocodeResult = null;

        /** Geocode using the supplied provider if valid */
        if (this.isRegistered(provider)) {
            geocodeResult = this.newInstance(provider).geocode(address);
        }
        /** If attempt failed, use the fallback providers if allowed */
        if (geocodeResult == null || (!geocodeResult.isSuccess() && useFallback)) {
            while ((geocodeResult == null || !geocodeResult.isSuccess()) && fallbackIterator.hasNext()) {
                geocodeResult = this.newInstance(fallbackIterator.next()).geocode(address);
            }
        }
        /** Ensure we don't return a null response */
        if (geocodeResult == null) {
            geocodeResult = new GeocodeResult(this.getClass(), ResultStatus.NO_GEOCODE_RESULT);
        }
        return geocodeResult;
    }

    /**
     * Perform batch geocoding using application defaults
     * @param addresses         List of addresses to geocode
     * @return                  List<GeocodeResult> corresponding to the addresses list.
     */
    public List<GeocodeResult> geocode(ArrayList<Address> addresses)
    {
        return this.geocode(addresses, DEFAULT_GEO_PROVIDER, DEFAULT_GEO_FALLBACK, true);
    }

    /**
     * Perform batch geocoding with default fallback option
     * @param addresses         List of addresses to geocode
     * @param provider          Provider to perform geocoding
     * @param useFallback       Set true to use default fallback
     * @return                  List<GeocodeResult> corresponding to the addresses list.
     */
    public List<GeocodeResult> geocode(ArrayList<Address> addresses, String provider, boolean useFallback)
    {
        return this.geocode(addresses, provider, DEFAULT_GEO_FALLBACK, useFallback);
    }

    /**
     * Perform batch geocoding with all specified parameters.
     * @param addresses         List of addresses to geocode
     * @param provider          Provider to perform geocoding
     * @param fallbackProviders Sequence of providers to fallback to
     * @param useFallback       Set true to use fallback
     * @return                  List<GeocodeResult> corresponding to the addresses list.
     */
    public List<GeocodeResult> geocode(ArrayList<Address> addresses, String provider,
                                       LinkedList<String> fallbackProviders, boolean useFallback)
    {
        logger.info("Performing batch geocode using provider: " + provider + " with fallback to: " +
                    fallbackProviders + " with fallback set to " + useFallback);

        /** Clone the list of fall back providers */
        LinkedList<String> fallback = (fallbackProviders != null) ? (LinkedList<String>) fallbackProviders.clone()
                                                                  : (LinkedList<String>) DEFAULT_GEO_FALLBACK.clone();

        Iterator<String> fallbackIterator = fallback.iterator();
        List<GeocodeResult> geocodeResults = null;
        List<Integer> failedIndices;

        /** Geocode using the supplied provider if valid */
        if (this.isRegistered(provider)) {
            geocodeResults = this.newInstance(provider).geocode(addresses);
        }
        else {
            logger.error("Supplied an empty geocoding provider!");
        }

        if (useFallback) {
            /** Get the indices of results that were not successful */
            failedIndices = getFailedResultIndices(geocodeResults);

            /** Create new batches containing just the failed results and run them through
             *  the fallback providers. Recompute the failed results and repeat until all
             *  fallback providers specified have been used. */
            while (!failedIndices.isEmpty() && fallbackIterator.hasNext()) {
                String fallbackProvider = fallbackIterator.next();
                logger.debug("Some missing geocodes exist. Falling back to " + fallbackProvider);

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
