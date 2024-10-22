package gov.nysenate.sage.service.geo;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.result.AddressResult;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.provider.geocache.GeoCache;
import gov.nysenate.sage.provider.geocode.GeocodeService;
import gov.nysenate.sage.provider.geocode.Geocoder;
import gov.nysenate.sage.service.address.AddressServiceProvider;
import gov.nysenate.sage.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Point of access for all geocoding requests. This class maintains a collection of available
 * geocoding providers and contains logic for distributing requests and collecting responses
 * from the providers.
 */
@Service
public class SageGeocodeServiceProvider implements GeocodeServiceProvider {
    private static final Logger logger = LoggerFactory.getLogger(SageGeocodeServiceProvider.class);

    /** Caching members */
    private final Map<Geocoder, GeocodeService> geocoderMap;
    private final GeoCache geocache;
    private final AddressServiceProvider addressServiceProvider;

    @Autowired
    public SageGeocodeServiceProvider(GeoCache geocache, List<GeocodeService> geocodeServices,
                                      AddressServiceProvider addressServiceProvider) {
        this.geocache = geocache;
        this.geocoderMap = geocodeServices.stream()
                .collect(Collectors.toMap(GeocodeService::name, Function.identity()));
        this.addressServiceProvider = addressServiceProvider;
    }

    /**
     * Perform a single geocode with all specified parameters.
     * @param address           Address to geocode
     * @param providers         Providers to perform geocoding
     * @return                  GeocodeResult
     */
    public GeocodeResult geocode(Address address, List<Geocoder> providers, boolean doNotCache) {
        boolean uspsValidated = false;
        var geocodeResult = new GeocodeResult(null, ResultStatus.NO_GEOCODE_RESULT);

        for (Geocoder geocoder : providers) {
            geocodeResult = geocoderMap.get(geocoder).geocode(address);
            if (geocodeResult.isSuccess()) {
                break;
            }
        }

        // Ensure we don't return a null response
        if (!geocodeResult.isSuccess()) {
            logger.warn("No valid geocode result.");
        }
        else {
            AddressResult addressResult = addressServiceProvider.validate(address, null, false);
            if (addressResult != null && addressResult.isValidated()) {
                geocodeResult.getGeocodedAddress().setAddress(addressResult.getAddress());
                uspsValidated = true;
            }

            // USPS address correction
            // Output result if log level is high enough
            if (logger.isInfoEnabled()) {
                Geocode gc = geocodeResult.getGeocode();
                Address addr = geocodeResult.getAddress();
                if (gc != null && addr != null) {
                    String source = (geocodeResult.getSource() != null) ? geocodeResult.getSource().toString() : "Missing";
                    logger.info("Geocode Result - Source: '{}', Quality: '{}', Lat/Lon: '{}', Address: '{}'", source, gc.quality(), gc.point(), addr);
                }
            }
        }

        geocodeResult.setResultTime(TimeUtil.currentTimestamp());
        if (!uspsValidated) {
            geocodeResult.setGeocodedAddress(new GeocodedAddress(address, geocodeResult.getGeocode()));
        }
        if (!doNotCache && geocodeResult.getGeocodedAddress().isValidGeocode()) {
            geocache.saveToCacheAndFlush(geocodeResult);
        }
        return geocodeResult;
    }

    /**
     * Perform batch geocoding with all specified parameters.
     * @param addresses         List of addresses to geocode
     * @param providers         Providers to perform geocoding
     * @return                  List<GeocodeResult> corresponding to the addresses list.
     */
    public List<GeocodeResult> geocode(List<Address> addresses, List<Geocoder> providers, boolean doNotCache) {
        final int addressCount = addresses.size();
        final List<GeocodeResult> finalResults = new ArrayList<>(addresses.size());
        for (Address address : addresses) {
            finalResults.add(new GeocodeResult(null, ResultStatus.NO_GEOCODE_RESULT,
                    new GeocodedAddress(address)));
        }

        // Clone the list of fall back providers
        logger.info("Performing {} geocodes.", addressCount);


        Set<Integer> invalidIndices = new HashSet<>();
        for (int i = 0; i < addressCount; i++) {
            if (addresses.get(i) == null || addresses.get(i).isEmpty()) {
                finalResults.set(i, new GeocodeResult(null, ResultStatus.MISSING_ADDRESS));
                invalidIndices.add(i);
            }
        }

        for (Geocoder provider : providers) {
            List<Integer> indicesToGeocode = new ArrayList<>();
            List<Address> addrsToGeocode = new ArrayList<>();
            for (int i = 0; i < finalResults.size(); i++) {
                GeocodeResult currResult = finalResults.get(i);
                if (!currResult.isSuccess() && currResult.getStatusCode() != ResultStatus.MISSING_ADDRESS) {
                    indicesToGeocode.add(i);
                    addrsToGeocode.add(finalResults.get(i).getAddress());
                }
            }
            List<GeocodeResult> results = geocoderMap.get(provider).geocode(addrsToGeocode);
            for (int i = 0; i < invalidIndices.size(); i++) {
                finalResults.set(indicesToGeocode.get(i), results.get(i));
            }
        }

        // If the geocodeResults do not align with the valid input address set, produce error and return error result.
        if (finalResults.size() != addresses.size()) {
            logger.error("Batch geocode result size not equal to address set size!");
            GeocodeResult errorResult = new GeocodeResult(null, ResultStatus.INTERNAL_ERROR);
            errorResult.addMessage("Batch response was not consistent with the input addresses list.");
            return List.of(errorResult);
        }

        if (!doNotCache) {
            geocache.saveToCacheAndFlush(finalResults);
        }
        return finalResults;
    }

    public Set<Geocoder> geocoders() {
        return geocoderMap.keySet();
    }
}
