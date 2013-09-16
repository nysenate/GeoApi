package gov.nysenate.sage.service.geo;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.address.GeocodedStreetAddress;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.util.FormatUtil;
import gov.nysenate.sage.util.TimeUtil;
import org.apache.log4j.Logger;

import java.sql.Timestamp;
import java.util.*;

import static gov.nysenate.sage.model.result.ResultStatus.*;

/**
* Utility class for validating geocode requests and responses.
*/
public abstract class GeocodeServiceValidator
{
    private static final Logger logger = Logger.getLogger(GeocodeServiceValidator.class);

    /** Keep track of GeocodeService implementations that are temporarily unavailable. */
    private static Map<Class<? extends GeocodeService>, Timestamp> frozenGeocoders = new HashMap<>();
    private static Map<Class<? extends GeocodeService>, Integer> failedRequests = new HashMap<>();
    private static Integer FAILURE_THRESHOLD = 20;
    private static Integer RETRY_INTERVAL_SECS = 300;

    public static boolean isGeocodeServiceActive(Class<? extends GeocodeService> geocodeService, GeocodeResult geocodeResult)
    {
        if (geocodeService != null) {
            if (frozenGeocoders.containsKey(geocodeService)) {
                /** Check if the freeze time has elapsed */
                Timestamp freezeTime = frozenGeocoders.get(geocodeService);
                Calendar refreshTime = Calendar.getInstance();
                refreshTime.add(Calendar.SECOND, RETRY_INTERVAL_SECS * -1);
                Timestamp refreshTimestamp = new Timestamp(refreshTime.getTimeInMillis());

                /** If the geocoder's freeze time has expired, remove it from the disabled list */
                if (freezeTime.compareTo(refreshTimestamp) < 0) {
                    frozenGeocoders.remove(geocodeService);
                    return true;
                }

                if (geocodeResult == null) {
                    geocodeResult = new GeocodeResult(geocodeService);
                }
                geocodeResult.setStatusCode(GEOCODE_PROVIDER_TEMP_DISABLED);
                geocodeResult.setResultTime(TimeUtil.currentTimestamp());
                logger.debug(geocodeService.getSimpleName() + " is currently disabled.");
                return false;
            }
            else {
                return true;
            }
        }
        else {
            return false;
        }
    }

    /**
     *
     * @param geocodeService GeocodeService that returned the successful result.
     */
    public static void recordSuccessResult(Class<? extends GeocodeService> geocodeService)
    {
        failedRequests.remove(geocodeService);
        frozenGeocoders.remove(geocodeService);
    }

    /**
     *
     * @param geocodeService GeocodeService that returned the failed result.
     */
    public static void recordFailedResult(Class<? extends GeocodeService> geocodeService)
    {
        if (geocodeService != null) {
            int failedCount = 0;
            if (failedRequests.containsKey(geocodeService)) {
                failedCount = failedRequests.get(geocodeService);
            }
            failedRequests.put(geocodeService, ++failedCount);

            /** If the failure count reaches the threshold, freeze the geocoder. */
            if (failedCount >= FAILURE_THRESHOLD) {
                frozenGeocoders.put(geocodeService, TimeUtil.currentTimestamp());
                logger.info("Freezing " + geocodeService.getSimpleName() + " due to consecutive failures.");
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
    public static boolean validateGeocodeResult(Class<? extends GeocodeService> source, GeocodedAddress geocodedAddress,
                                                GeocodeResult geocodeResult, Boolean freeze)
    {
        if (geocodedAddress != null) {
            geocodeResult.setGeocodedAddress(geocodedAddress);
            if (!geocodedAddress.isValidGeocode()){
                geocodeResult.setStatusCode(NO_GEOCODE_RESULT);
                if (geocodedAddress.getGeocode() != null) {
                    logger.trace("Geocode Response: " + FormatUtil.toJsonString(geocodedAddress.getGeocode()));
                }
                recordFailedResult(source);
                return false;
            }
            geocodeResult.setStatusCode(SUCCESS);
            recordSuccessResult(source);
            return true;
        }
        geocodeResult.setStatusCode(RESPONSE_PARSE_ERROR);
        recordFailedResult(source);
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
    public static boolean validateGeocodeResult(Class<? extends GeocodeService> source, GeocodedStreetAddress geoStreetAddress, GeocodeResult geocodeResult, Boolean freeze)
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
     *
     * @param source GeocodeService implementation that provided the results.
     * @param addresses List of input addresses to check results against.
     * @param geocodeResults List of GeocodeResults to store results in.
     * @param geocodedAddresses List of GeocodedAddresses
     * @param freeze If true the geocode service may be temporarily disabled for a specified duration
     * if it keeps returning error results.
     * @return GeocodeResult
     */
    public static boolean validateBatchGeocodeResult(Class<? extends GeocodeService> source, ArrayList<Address> addresses, ArrayList<GeocodeResult> geocodeResults,
                                                      List<GeocodedAddress> geocodedAddresses, Boolean freeze)
    {
        /** Make sure the result array is empty at first */
        geocodeResults.clear();

        /** Check each geocoded address to set the result status accordingly */
        if (geocodedAddresses != null && geocodedAddresses.size() == addresses.size()) {
            for (GeocodedAddress geocodedAddress : geocodedAddresses) {
                GeocodeResult geocodeResult = new GeocodeResult(source);
                validateGeocodeResult(source, geocodedAddress, geocodeResult, freeze);
                geocodeResults.add(geocodeResult);
            }
            return true;
        }
        /** If the batch is invalid, return a collection of invalidated geocode results. */
        else {
            logger.warn("Invalidating this batch! The results returned do not match the addresses given.");
            for (Address a : addresses) {
                geocodeResults.add(new GeocodeResult(source, NO_GEOCODE_RESULT, new GeocodedAddress(a)));
            }
        }
        return false;
    }
}
