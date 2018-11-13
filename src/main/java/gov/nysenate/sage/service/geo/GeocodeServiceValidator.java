package gov.nysenate.sage.service.geo;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.address.GeocodedStreetAddress;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.util.Config;
import gov.nysenate.sage.util.FormatUtil;
import gov.nysenate.sage.util.TimeUtil;
import org.springframework.stereotype.Service;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static gov.nysenate.sage.model.result.ResultStatus.*;

/**
* Utility class for validating geocode requests and responses.
*/
@Service
public abstract class GeocodeServiceValidator extends BaseDao
{


    private static final Logger logger = LoggerFactory.getLogger(GeocodeServiceValidator.class);
    private Config config = getConfig();

    /** Keep track of GeocodeService implementations that are temporarily unavailable. */
    private static Set<Class<? extends GeocodeService>> activeGeocoders = new HashSet<>();
    private static Map<Class<? extends GeocodeService>, Timestamp> frozenGeocoders = new ConcurrentHashMap<>();
    private static Map<Class<? extends GeocodeService>, Integer> failedRequests = new ConcurrentHashMap<>();
    private Integer FAILURE_THRESHOLD = Integer.parseInt(config.getValue("geocoder.failure.threshold", "20"));
    private Integer RETRY_INTERVAL_SECS = Integer.parseInt(config.getValue("geocoder.retry.interval", "300"));

    /**
     * Designate the geocodeService class as an active geocoder. Note that an active geocoder may
     * still be blocked if it fails to return valid responses over a period of time.
     * @param geocodeService GeocodeService implementation class to mark as active.
     */
    public static void setGeocoderAsActive(Class<? extends GeocodeService> geocodeService)
    {
        activeGeocoders.add(geocodeService);
    }

    /**
     * Determine if geocode service is active by checking if it's been blocked due to failed requests.
     * @param geocodeService GeocodeService to check
     * @param geocodeResult GeocodeResult to set the result status if the service is in fact disabled.
     * @return True if geocodeService is active, false otherwise.
     */
    public boolean isGeocodeServiceActive(Class<? extends GeocodeService> geocodeService, GeocodeResult geocodeResult)
    {
        if (geocodeResult == null) {
            geocodeResult = new GeocodeResult(geocodeService);
        }

        if (geocodeService != null) {
            if (!activeGeocoders.contains(geocodeService)) {
                logger.debug(geocodeService.getSimpleName() + " is disabled by configuration.");
                geocodeResult.setStatusCode(GEOCODE_PROVIDER_DISABLED);
                geocodeResult.setResultTime(TimeUtil.currentTimestamp());
                return false;
            }
            else if (frozenGeocoders.containsKey(geocodeService)) {
                /** Check if the freeze time has elapsed */
                Timestamp freezeTime = frozenGeocoders.get(geocodeService);
                Calendar refreshTime = Calendar.getInstance();
                refreshTime.add(Calendar.SECOND, RETRY_INTERVAL_SECS * -1);
                Timestamp refreshTimestamp = new Timestamp(refreshTime.getTimeInMillis());

                /** If the geocoder's freeze time has expired, remove it from the disabled list */
                if (freezeTime.compareTo(refreshTimestamp) < 0) {
                    frozenGeocoders.remove(geocodeService);
                    failedRequests.remove(geocodeService);
                    return true;
                }

                geocodeResult.setStatusCode(GEOCODE_PROVIDER_TEMP_DISABLED);
                geocodeResult.setResultTime(TimeUtil.currentTimestamp());
                logger.debug(geocodeService.getSimpleName() + " is temporarily disabled.");
                return false;
            }
            else {
                return true;
            }
        }
        geocodeResult.setStatusCode(PROVIDER_NOT_SUPPORTED);
        geocodeResult.setResultTime(TimeUtil.currentTimestamp());
        return false;
    }

    /**
     * Determine if geocode service is active by checking if it's been blocked due to failed requests.
     * This method should be called prior to geocoding a batch set of addresses.
     * @param geocodeService GeocodeService to check
     * @param geocodeResultList GeocodeResult to set the result status if the service is in fact disabled.
     * @param inputSize The number of failed results to set in geocodeResultList if service is not active.
     * @return True if geocodeService is active, false otherwise.
     */
    public boolean isGeocodeServiceActive(Class<? extends GeocodeService> geocodeService, List<GeocodeResult> geocodeResultList, int inputSize)
    {
        /** If the geocode service is not active, populate the geocodeResults list with results indicating the
         * disabled geocoder status. */
        GeocodeResult result = new GeocodeResult(geocodeService);
        if (!isGeocodeServiceActive(geocodeService, result)) {
            geocodeResultList.clear();
            for (int i = 0; i < inputSize; i++) {
                geocodeResultList.add(new GeocodeResult(geocodeService, result.getStatusCode()));
            }
            return false;
        }
        /** Otherwise the geocode service is active. */
        return true;
    }

    /**
     * Will remove the geocoder from it's frozen state.
     * @param geocodeService GeocodeService that returned the successful result.
     */
    public static void removeGeocoderBlock(Class<? extends GeocodeService> geocodeService)
    {
        failedRequests.remove(geocodeService);
        frozenGeocoders.remove(geocodeService);
    }

    /**
     * If the number of consecutive failed results equals FAILURE_THRESHOLD, add the geocode service
     * to the frozen list, thus making it temporarily disabled.
     * @param geocodeService GeocodeService that returned the failed result.
     */
    public synchronized void recordFailedResult(Class<? extends GeocodeService> geocodeService)
    {
        if (geocodeService != null) {
            int failedCount = 0;
            if (failedRequests.containsKey(geocodeService)) {
                failedCount = failedRequests.get(geocodeService);
            }
            failedRequests.put(geocodeService, ++failedCount);

            /** If the failure count reaches the threshold, freeze the geocoder if not already. */
            if (failedCount >= FAILURE_THRESHOLD && !frozenGeocoders.containsKey(geocodeService)) {
                frozenGeocoders.put(geocodeService, TimeUtil.currentTimestamp());
                logger.info("Temporarily blocking " + geocodeService.getSimpleName() + " for " + RETRY_INTERVAL_SECS + " secs due to consecutive failures.");
            }
        }
    }

    /**
     * Basic null checks on geocode inputs. Sets errors to the geocode result
     * @param address       Address to validate
     * @param geocodeResult GeocodeResult to set status codes to
     * @return              True if valid input, false otherwise
     */
    public static boolean validateGeocodeInput(Address address, GeocodeResult geocodeResult)
    {
        if (address == null) {
            geocodeResult.setStatusCode(MISSING_ADDRESS);
            return false;
        }
        else if (address.isEmpty()){
            geocodeResult.setStatusCode(INSUFFICIENT_ADDRESS);
            return false;
        }
        return true;
    }

    /**
     * Perform validation on a GeocodedAddress that is meant to be encapsulated in a GeocodeResult.
     * If validation fails the status code on the result object will be set.
     * @param source GeocodeService implementation that provided the result.
     * @param geocodedAddress The resulting GeocodedAddress
     * @param geocodeResult The GeocodeResult to set
     * @return True if valid GeocodedAddress, false otherwise
     */
    public boolean validateGeocodeResult(Class<? extends GeocodeService> source, GeocodedAddress geocodedAddress,
                                                GeocodeResult geocodeResult, Boolean freeze)
    {
        if (geocodedAddress != null) {
            geocodeResult.setGeocodedAddress(geocodedAddress);
            if (!geocodedAddress.isValidGeocode()){
                geocodeResult.setStatusCode(NO_GEOCODE_RESULT);
                if (geocodedAddress.getGeocode() != null) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Geocode Response: " + FormatUtil.toJsonString(geocodedAddress.getGeocode()));
                    }
                }
                if (freeze) {
                    recordFailedResult(source);
                }
                return false;
            }
            geocodeResult.setStatusCode(SUCCESS);
            removeGeocoderBlock(source);
            return true;
        }
        geocodeResult.setStatusCode(RESPONSE_PARSE_ERROR);
        if (freeze) {
            recordFailedResult(source);
        }
        return false;
    }

    /**
     * Perform validation on a GeocodedStreetAddress.
     * @param source GeocodeService implementation that provided the result.
     * @param geoStreetAddress The resulting GeocodedStreetAddress
     * @param geocodeResult    The GeocodeResult to set
     * @param freeze If true the geocode service may be temporarily disabled for a specified duration
     *               if it keeps returning error results.
     * @return GeocodeResult
     */
    public boolean validateGeocodeResult(Class<? extends GeocodeService> source, GeocodedStreetAddress geoStreetAddress, GeocodeResult geocodeResult, Boolean freeze)
    {
        Boolean success = false;
        if (geoStreetAddress != null) {
            success = validateGeocodeResult(source, geoStreetAddress.toGeocodedAddress(), geocodeResult, freeze);
        }
        if (!success) {
            geocodeResult.setStatusCode(NO_GEOCODE_RESULT);
        }
        return success;
    }

    /**
     * Perform validation on a list of GeocodedAddresses. The entire batch is considered one request so
     * if freeze is true, a single failed attempt will be recorded if no successful results exist.
     * @param source GeocodeService implementation that provided the results.
     * @param addresses List of input addresses to check results against.
     * @param geocodeResults List of GeocodeResults to store results in.
     * @param geocodedAddresses List of GeocodedAddresses
     * @param freeze If true the geocode service may be temporarily disabled for a specified duration
     * if it keeps returning error results.
     * @return GeocodeResult
     */
    public boolean validateBatchGeocodeResult(Class<? extends GeocodeService> source, ArrayList<Address> addresses, ArrayList<GeocodeResult> geocodeResults,
                                                      List<GeocodedAddress> geocodedAddresses, Boolean freeze)
    {
        boolean hasValidResult = false;

        /** Make sure the result array is empty at first */
        geocodeResults.clear();

        /** Check each geocoded address to set the result status accordingly */
        if (geocodedAddresses != null && geocodedAddresses.size() == addresses.size()) {
            for (GeocodedAddress geocodedAddress : geocodedAddresses) {
                GeocodeResult geocodeResult = new GeocodeResult(source);
                if (validateGeocodeResult(source, geocodedAddress, geocodeResult, false)) {
                    hasValidResult = true;
                }
                geocodeResults.add(geocodeResult);
            }
        }
        /** If the batch is invalid, return a collection of invalidated geocode results. */
        else {
            logger.warn("Invalidating this batch! The results returned do not match the addresses given.");
            for (Address a : addresses) {
                geocodeResults.add(new GeocodeResult(source, NO_GEOCODE_RESULT, new GeocodedAddress(a)));
            }
        }

        if (hasValidResult) {
            removeGeocoderBlock(source);
        }
        else if (freeze) {
            recordFailedResult(source);
        }
        return hasValidResult;
    }

    public Integer getFAILURE_THRESHOLD() {
        return FAILURE_THRESHOLD;
    }

    public Integer getRETRY_INTERVAL_SECS() {
        return RETRY_INTERVAL_SECS;
    }
}