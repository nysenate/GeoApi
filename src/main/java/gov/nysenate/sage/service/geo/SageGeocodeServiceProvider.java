package gov.nysenate.sage.service.geo;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.api.BatchGeocodeRequest;
import gov.nysenate.sage.model.api.GeocodeRequest;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.provider.geocode.GeocodeService;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public interface SageGeocodeServiceProvider {

    /**
     * Designates a provider (that has been registered) as a reliable source for caching results.
     * @param provider Same providerName used when registering the provider
     */
    public void registerProviderAsCacheable(GeocodeService provider);

    /**
     * Performs single geocode using GeocodeRequest.
     * @param geocodeRequest GeocodeRequest object with geocode options and address set.
     * @return GeocodeResult
     */
    public GeocodeResult geocode(GeocodeRequest geocodeRequest);

    /**
     * Perform a single geocode using the application defaults.
     * @param address   Address to geocode
     * @return          GeocodeResult
     */
    public GeocodeResult geocode(Address address);

    /**
     * Perform a single geocode with optional default fallback mode.
     * @param address       Address to geocode
     * @param provider      Provider to perform geocoding
     * @param useFallback   Set true to use default fallback
     * @return              GeocodeResult
     */
    public GeocodeResult geocode(Address address, String provider, boolean useFallback);

    /**
     * Perform a single geocode with caching and an optional default fallback mode.
     * @param address       Address to geocode
     * @param provider      Provider to perform geocoding
     * @param useFallback   Set true to use default fallback
     * @return              GeocodeResult
     */
    public GeocodeResult geocode(Address address, String provider, boolean useFallback, boolean useCache);

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
                                 boolean useCache, boolean doNotCache);

    /**
     * Perform batch geocoding using supplied BatchGeocodeRequest
     * @param batchGeoRequest BatchGeocodeRequest with desired fields set
     * @return  List<GeocodeResult> corresponding to the addresses list
     */
    public List<GeocodeResult> geocode(BatchGeocodeRequest batchGeoRequest);

    /**
     * Perform batch geocoding using recommended application defaults
     * @param addresses         List of addresses to geocode
     * @return                  List<GeocodeResult> corresponding to the addresses list.
     */
    public List<GeocodeResult> geocode(List<Address> addresses);

    /**
     * Perform batch geocoding with default fallback option
     * @param addresses         List of addresses to geocode
     * @param provider          Provider to perform geocoding
     * @param useFallback       Set true to use default fallback
     * @param useCache          Set true to attempt cache lookup first
     * @return                  List<GeocodeResult> corresponding to the addresses list.
     */
    public List<GeocodeResult> geocode(List<Address> addresses, String provider, boolean useFallback, boolean useCache);

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
                                       List<String> fallbackProviders, boolean useFallback, boolean useCache);

    /**
     * Return a map containing a String and its Geocode Service.
     * @return
     */
    public Map<String,GeocodeService> getActiveGeoProviders();

    /**
     * Return a map containing a String and its Geocode Service. This is used on the front end to display the
     * geoproviders we can currently contact
     * @return
     */
    public Map<String, Class<? extends GeocodeService>> getActiveGeocoderClassMap();

}
